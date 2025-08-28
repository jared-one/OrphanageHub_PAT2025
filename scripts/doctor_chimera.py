#!/usr/bin/env python3
"""
Doctor Chimera - Advanced Java Diagnostic & Repair System
- Full-project javac with -sourcepath (cross-file types resolve)
- Classpath from section build + Maven deps
- Fixers:
  * Missing semicolon (skips method/class headers)
  * Missing braces (adds { or } safely)
  * Remove parentheses from class headers: `class X()` -> `class X`
  * Remove parentheses in throws: `throws Ex()` -> `throws Ex`
  * Missing import (common + project search)
  * Unclosed string
  * Remove noise lines of only ))) or similar
  * Remove stray bare */ lines
  * New: Fix extra semicolons causing illegal start
  * New: Fix mismatched quotes
  * New: Fix ')' or ',' expected in method calls using column position
- Modes: diagnose, interactive, fix, watch, report
"""
import argparse, os, re, shutil, subprocess, sys, tempfile, time, sqlite3
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path
from typing import List, Optional, Tuple, Dict, Set

try:
    from rich.console import Console
    from rich.panel import Panel
    from rich.prompt import Confirm, Prompt
    from rich.progress import Progress, SpinnerColumn, TextColumn
except ImportError:
    print("ERROR: Missing 'rich'. Run: pip install rich", file=sys.stderr)
    sys.exit(1)

# optional
try:
    from watchdog.observers import Observer
    from watchdog.events import FileSystemEventHandler
    HAS_WATCHDOG = True
except Exception:
    HAS_WATCHDOG = False

ROOT = Path(__file__).resolve().parent.parent
SRC_DIR = ROOT / "src" / "main" / "java"
TARGET_DIR = ROOT / "target"
SECTION_CLASSES = TARGET_DIR / "section-classes"
BACKUP_DIR = TARGET_DIR / "doctor_backups"
DB_PATH = TARGET_DIR / "doctor_chimera.db"
console = Console()

@dataclass(frozen=True)
class CompilationError:
    file_path: Path
    line: int
    column: int
    message: str
    raw: str

@dataclass
class Fix:
    error: CompilationError
    description: str
    changes: List[Tuple[int, str, str]]  # (line, old, new); line=0 for insert at top, line=len+1 for append
    confidence: float
    fix_type: str

def os_pathsep() -> str:
    return ";" if os.name == "nt" else ":"

def read_file_lines(p: Path) -> List[str]:
    return p.read_text(encoding="utf-8", errors="ignore").splitlines()

def write_file_lines(p: Path, lines: List[str]) -> None:
    p.write_text("\n".join(lines) + "\n", encoding="utf-8")

def ensure_dirs():
    BACKUP_DIR.mkdir(parents=True, exist_ok=True)
    TARGET_DIR.mkdir(parents=True, exist_ok=True)
    (ROOT / "logs").mkdir(exist_ok=True)

def backup_file(p: Path) -> Path:
    ts = datetime.now().strftime("%Y%m%d_%H%M%S")
    b = BACKUP_DIR / f"{p.stem}_{ts}{p.suffix}"
    shutil.copy2(p, b)
    return b

def get_mavenw() -> str:
    return "./mvnw.cmd" if os.name == "nt" else "./mvnw"

def get_section_classpath() -> Optional[str]:
    cp = []
    if SECTION_CLASSES.exists(): cp.append(str(SECTION_CLASSES))
    if (TARGET_DIR / "classes").exists(): cp.append(str(TARGET_DIR / "classes"))
    ext = TARGET_DIR / "ext-cp.txt"
    if ext.exists():
        s = ext.read_text(encoding="utf-8").strip()
        if s: cp.append(s)
    return os_pathsep().join(cp) if cp else None

def build_maven_classpath() -> str:
    cmd = [
        get_mavenw(), "-q", "-B", "dependency:build-classpath",
        "-Dmdep.outputFile=target/ext-cp.txt",
        "-Dmdep.includeScope=compile",
        "-Dmdep.outputAbsoluteArtifactFilename=true",
        f"-Dmdep.pathSeparator={os_pathsep()}",
    ]
    subprocess.run(cmd, cwd=ROOT, check=False, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    cp = get_section_classpath()
    if cp: return cp
    parts = []
    if SECTION_CLASSES.exists(): parts.append(str(SECTION_CLASSES))
    if (TARGET_DIR / "classes").exists(): parts.append(str(TARGET_DIR / "classes"))
    return os_pathsep().join(parts) if parts else "."

def get_classpath() -> str:
    return get_section_classpath() or build_maven_classpath()

class JavaCompiler:
    def __init__(self):
        self.javac = shutil.which("javac")
        if not self.javac:
            raise RuntimeError("javac not found. Install JDK 17 and ensure PATH.")
        self.classpath = get_classpath()

    def compile_file(self, path: Path) -> List[CompilationError]:
        with tempfile.TemporaryDirectory() as td:
            cmd = [self.javac, "-d", td, "-cp", self.classpath, "-sourcepath", str(SRC_DIR), "-Xlint:all", str(path)]
            proc = subprocess.run(cmd, cwd=ROOT, text=True, capture_output=True)
            return self._parse(proc.stdout + proc.stderr)

    def compile_all(self) -> List[CompilationError]:
        files = [str(p) for p in SRC_DIR.rglob("*.java") if p.is_file()]
        if not files: return []
        with tempfile.TemporaryDirectory() as td:
            cmd = [self.javac, "-d", td, "-cp", self.classpath, "-sourcepath", str(SRC_DIR), "-Xlint:all", *files]
            proc = subprocess.run(cmd, cwd=ROOT, text=True, capture_output=True)
            return self._parse(proc.stdout + proc.stderr)

    def _parse(self, text: str) -> List[CompilationError]:
        if not text: return []
        entries: List[CompilationError] = []
        line_re = re.compile(r"^(?P<file>.*?\.java):(?P<line>\d+): (?P<kind>error|warning): (?P<msg>.*)$")
        caret_re = re.compile(r"^\s*\^$")
        lines = text.splitlines()
        i = 0
        while i < len(lines):
            m = line_re.match(lines[i].strip())
            if m:
                fp = Path(m.group("file"))
                if not fp.is_absolute(): fp = (ROOT / fp).resolve()
                ln = int(m.group("line")); col = 1; raw = lines[i]  # default col 1
                if i+2 < len(lines) and caret_re.match(lines[i+2]):
                    caret_line = lines[i+2]
                    caret_pos = caret_line.find('^')
                    if caret_pos != -1:
                        col = caret_pos + 1
                    raw = "\n".join(lines[i:i+3]); i += 2
                entries.append(CompilationError(fp, ln, col, m.group("msg").strip(), raw))
            i += 1
        return entries

# Fixers
class Fixer:
    def can_fix(self, e: CompilationError) -> bool: ...
    def generate(self, e: CompilationError, content: List[str]) -> Optional[Fix]: ...

def is_method_or_class_header(s: str) -> bool:
    t = s.strip()
    if t.endswith("{"): return True
    if t.startswith(("public","private","protected","static","final","abstract")) and "(" in t and (t.endswith(")") or t.endswith("){") or t.endswith(") {")):
        return True
    if re.search(r"\b(class|interface|enum|record)\b", t) and "(" in t:  # malformed class-like with ()
        return True
    return False

class MissingSemicolonFixer(Fixer):
    def can_fix(self, e: CompilationError) -> bool:
        return "';' expected" in e.message
    def generate(self, e: CompilationError, content: List[str]) -> Optional[Fix]:
        if not (1 <= e.line <= len(content)): return None
        old = content[e.line-1]
        if is_method_or_class_header(old): return None  # skip headers
        if old.strip().endswith(";"): return None
        pos = e.column - 1
        if 0 <= pos < len(old) and old[pos] not in ' ;':
            new = old[:pos] + ';' + old[pos:]
            return Fix(e, "Add missing semicolon at position", [(e.line, old, new)], 0.95, "position")
        return Fix(e, "Add missing semicolon", [(e.line, old, old.rstrip() + ";")], 0.90, "automatic")

class BracesFixer(Fixer):
    def can_fix(self, e: CompilationError) -> bool:
        return ("'{' expected" in e.message) or ("'}' expected" in e.message) or ("illegal start of" in e.message)
    def generate(self, e: CompilationError, content: List[str]) -> Optional[Fix]:
        if "'{' expected" in e.message and 1 <= e.line <= len(content):
            old = content[e.line-1]
            # if class header mistakenly has () remove them first
            hdr = re.sub(r"(\b(class|interface|enum|record)\s+[A-Za-z_]\w*)\s*\(\s*\)", r"\1", old)
            if hdr != old:
                return Fix(e, "Remove () from class header and add {", [(e.line, old, hdr.rstrip() + " {")], 0.90, "pattern")
            if not old.strip().endswith("{"):
                return Fix(e, "Add missing opening brace", [(e.line, old, old.rstrip() + " {")], 0.80, "automatic")
        if "'}' expected" in e.message:
            indent = ""
            if 1 < e.line <= len(content):
                m = re.match(r"^(\s*)", content[e.line-2]); indent = m.group(1) if m else ""
            return Fix(e, "Add missing closing brace", [(len(content)+1, "", indent + "}")], 0.75, "automatic")
        return None

class ClassHeaderParenFixer(Fixer):
    def can_fix(self, e: CompilationError) -> bool:
        return "'{' expected" in e.message
    def generate(self, e: CompilationError, content: List[str]) -> Optional[Fix]:
        if not (1 <= e.line <= len(content)): return None
        old = content[e.line-1]
        if re.search(r"\b(class|interface|enum|record)\b", old) and re.search(r"\(\s*\)", old):
            new = re.sub(r"(\b(class|interface|enum|record)\s+[A-Za-z_]\w*)\s*\(\s*\)", r"\1", old)
            if not new.strip().endswith("{"): new = new.rstrip() + " {"
            return Fix(e, "Fix class header () and add {", [(e.line, old, new)], 0.92, "pattern")
        return None

class ThrowsParensFixer(Fixer):
    def can_fix(self, e: CompilationError) -> bool:
        return ("';' expected" in e.message or "')' expected" in e.message or "illegal start" in e.message)
    def generate(self, e: CompilationError, content: List[str]) -> Optional[Fix]:
        if not (1 <= e.line <= len(content)): return None
        old = content[e.line-1]
        if "throws" in old and re.search(r"throws\s+[A-Za-z_][A-Za-z0-9_.]*\s*\(\s*\)", old):
            new = re.sub(r"(throws\s+[A-Za-z_][A-Za-z0-9_.]*)\s*\(\s*\)", r"\1", old)
            return Fix(e, "Remove () after exception in throws", [(e.line, old, new)], 0.90, "pattern")
        return None

class MissingImportFixer(Fixer):
    COMMON = {
        "List":"java.util.List","ArrayList":"java.util.ArrayList","Map":"java.util.Map","HashMap":"java.util.HashMap",
        "Set":"java.util.Set","HashSet":"java.util.HashSet","Optional":"java.util.Optional","Stream":"java.util.stream.Stream",
        "Collectors":"java.util.stream.Collectors","Logger":"org.slf4j.Logger","LoggerFactory":"org.slf4j.LoggerFactory",
        "LocalDate":"java.time.LocalDate","LocalDateTime":"java.time.LocalDateTime","Files":"java.nio.file.Files","Path":"java.nio.file.Path",
        "Timestamp":"java.sql.Timestamp","UUID":"java.util.UUID","JOptionPane":"javax.swing.JOptionPane",
        "BorderFactory":"javax.swing.BorderFactory","EmptyBorder":"javax.swing.border.EmptyBorder","CompoundBorder":"javax.swing.border.CompoundBorder",
        "MouseAdapter":"java.awt.event.MouseAdapter","GradientPaint":"java.awt.GradientPaint","Color":"java.awt.Color",
        "Font":"java.awt.Font","DefaultTableModel":"javax.swing.table.DefaultTableModel","JTable":"javax.swing.JTable",
        "JScrollPane":"javax.swing.JScrollPane","JButton":"javax.swing.JButton","JLabel":"javax.swing.JLabel",
        "JPanel":"javax.swing.JPanel","UIManager":"javax.swing.UIManager","User":"com.orphanagehub.model.User",
        "RenderingHints":"java.awt.RenderingHints"
    }
    def can_fix(self, e: CompilationError) -> bool:
        return "cannot find symbol" in e.message
    def generate(self, e: CompilationError, content: List[str]) -> Optional[Fix]:
        m = re.search(r"symbol:\s+(?:class|variable)\s+(\w+)", e.raw)
        if not m: return None
        cls = m.group(1)
        fq = self.COMMON.get(cls)
        if not fq:
            for java in SRC_DIR.rglob(f"{cls}.java"):
                rel = java.relative_to(SRC_DIR); pkg = str(rel.parent).replace(os.sep, ".")
                fq = f"{pkg}.{cls}" if pkg != "." else cls; break
        if not fq: return None
        imp = f"import {fq};"
        last_import = 0; package_line = 0; imports = set()
        for i, line in enumerate(content, 1):
            s = line.strip()
            if s.startswith("package "): package_line = i
            if s.startswith("import "): last_import = i; imports.add(s)
        if imp in imports: return None  # already imported
        insert_at = (last_import or package_line) + 1 if (last_import or package_line) else 1
        return Fix(e, f"Add import for {cls}", [(insert_at, "", imp)], 0.85, "automatic")

class UnclosedStringFixer(Fixer):
    def can_fix(self, e: CompilationError) -> bool:
        return "unclosed string literal" in e.message
    def generate(self, e: CompilationError, content: List[str]) -> Optional[Fix]:
        if not (1 <= e.line <= len(content)): return None
        old = content[e.line-1]
        if old.count('"') % 2 == 1 and not old.strip().endswith('"'):
            return Fix(e, "Close unclosed string", [(e.line, old, old.rstrip() + '"')], 0.75, "automatic")
        return None

class NoiseCloserLineFixer(Fixer):
    def can_fix(self, e: CompilationError) -> bool:
        return "illegal start" in e.message or "class, interface, enum" in e.message or "not a statement" in e.message
    def generate(self, e: CompilationError, content: List[str]) -> Optional[Fix]:
        if not (1 <= e.line <= len(content)): return None
        old = content[e.line-1].strip()
        if re.fullmatch(r"[\)};\s]+", old):
            return Fix(e, "Remove stray close parens/braces/semicolon line", [(e.line, content[e.line-1], "")], 0.80, "pattern")
        if old == "*/":
            return Fix(e, "Remove stray comment closer", [(e.line, content[e.line-1], "")], 0.80, "pattern")
        return None

class ExtraSemicolonFixer(Fixer):
    def can_fix(self, e: CompilationError) -> bool:
        return "illegal start of expression" in e.message or "not a statement" in e.message
    def generate(self, e: CompilationError, content: List[str]) -> Optional[Fix]:
        if not (1 <= e.line <= len(content)): return None
        old = content[e.line-1]
        if old.strip().endswith(";,") or re.search(r";\s*;", old):
            new = re.sub(r";\s*;", ";", old).rstrip(";,")
            return Fix(e, "Remove extra semicolon or comma", [(e.line, old, new)], 0.85, "pattern")
        pos = e.column - 1
        if 0 <= pos < len(old) and old[pos] in ';,)':
            new = old[:pos] + old[pos+1:]
            return Fix(e, "Remove extra punctuation at position", [(e.line, old, new)], 0.88, "position")
        return None

class MismatchedQuotesFixer(Fixer):
    def can_fix(self, e: CompilationError) -> bool:
        return "unclosed character literal" in e.message or "empty character literal" in e.message
    def generate(self, e: CompilationError, content: List[str]) -> Optional[Fix]:
        if not (1 <= e.line <= len(content)): return None
        old = content[e.line-1]
        if old.count("'") % 2 == 1:
            return Fix(e, "Fix mismatched single quotes", [(e.line, old, old + "'")], 0.70, "automatic")
        return None

class ParenOrCommaFixer(Fixer):
    def can_fix(self, e: CompilationError) -> bool:
        return "')' or ',' expected" in e.message
    def generate(self, e: CompilationError, content: List[str]) -> Optional[Fix]:
        if not (1 <= e.line <= len(content)): return None
        old = content[e.line-1]
        pos = e.column - 1  # 0-index insert point
        if pos < 0 or pos > len(old): pos = len(old)
        # Prioritize adding ) if pointing to ; or end
        if pos < len(old) and old[pos] in '; ) } ]' or pos == len(old):
            new = old[:pos] + ')' + old[pos:]
            return Fix(e, "Add missing closing parenthesis at position", [(e.line, old, new)], 0.95, "position")
        # For comma, if pointing to start of next arg (word)
        if pos < len(old) and re.match(r"\s*\w", old[pos:]):
            new = old[:pos] + ',' + old[pos:]
            return Fix(e, "Add missing comma in arguments at position", [(e.line, old, new)], 0.80, "position")
        return None

class Doctor:
    def __init__(self, mode: str):
        ensure_dirs()
        self.mode = mode
        self.compiler = JavaCompiler()
        self.fixers: List[Fixer] = [
            ClassHeaderParenFixer(),
            ThrowsParensFixer(),
            MissingSemicolonFixer(),
            BracesFixer(),
            MissingImportFixer(),
            UnclosedStringFixer(),
            NoiseCloserLineFixer(),
            ExtraSemicolonFixer(),
            MismatchedQuotesFixer(),
            ParenOrCommaFixer(),
        ]
        self.db = sqlite3.connect(DB_PATH)
        self._init_db()

    def _init_db(self):
        self.db.execute("""
            CREATE TABLE IF NOT EXISTS fixes(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              ts DATETIME DEFAULT CURRENT_TIMESTAMP,
              file TEXT,message TEXT,fix_type TEXT,confidence REAL,success INTEGER
            )
        """); self.db.commit()

    def run(self) -> bool:
        console.print(Panel.fit(f"🩺 Doctor Chimera\nMode: [yellow]{self.mode}[/yellow]", border_style="cyan"))
        if self.mode == "diagnose": return self._diagnose()
        if self.mode == "interactive": return self._interactive()
        if self.mode == "fix": return self._auto_fix()
        if self.mode == "watch": return self._watch()
        if self.mode == "report": return self._report()
        return False

    def _diagnose(self) -> bool:
        errs = self.compiler.compile_all()
        self._print_errors(errs)
        console.print(f"[yellow]Found {len(errs)} error(s)[/yellow]")
        return len(errs) == 0

    def _interactive(self) -> bool:
        max_iters = 20  # increased limit
        for it in range(max_iters):
            errs = self.compiler.compile_all()
            if not errs:
                console.print("[green]✅ All errors fixed[/green]"); return True
            console.print(f"[cyan]Iteration {it+1}/{max_iters}[/cyan]")
            for e in errs:
                if not self._handle(e): return False
        console.print("[yellow]Max iterations reached[/yellow]")
        return False

    def _auto_fix(self) -> bool:
        max_iters = 20
        for it in range(max_iters):
            errs = self.compiler.compile_all()
            if not errs:
                console.print("[green]✅ All errors fixed[/green]"); return True
            console.print(f"[cyan]Iteration {it+1}/{max_iters}[/cyan]")
            changed = False
            for e in errs:
                fx = self._find_fix(e)
                if fx and fx.confidence >= 0.80:
                    if self._apply(fx): changed = True
            if not changed:
                console.print("[yellow]No more high-confidence fixes[/yellow]")
                self._print_errors(errs)
                return False
        console.print("[yellow]Max iterations reached in auto-fix[/yellow]")
        return False

    def _watch(self) -> bool:
        if not HAS_WATCHDOG:
            console.print("[yellow]Install watchdog: pip install watchdog[/yellow]"); return False
        console.print("[cyan]👁 Watching src/main/java (Ctrl+C to stop)[/cyan]")
        class Handler(FileSystemEventHandler):
            def __init__(self, cb): self.cb = cb
            def on_modified(self, e):
                if not e.is_directory and e.src_path.endswith(".java"): self.cb(Path(e.src_path))
            def on_created(self, e):
                if not e.is_directory and e.src_path.endswith(".java"): self.cb(Path(e.src_path))
        def on_change(p: Path):
            console.print(f"\n[blue]Changed:[/blue] {p.relative_to(ROOT)}")
            errs = self.compiler.compile_file(p)
            if not errs: console.print("[green]✓ Clean[/green]"); return
            self._print_errors(errs)
            changed = False
            for e in errs:
                fx = self._find_fix(e)
                if fx and fx.confidence >= 0.80:
                    if self._apply(fx): changed = True
            if changed:
                console.print("[green]Rechecking after fixes...[/green]")
                if not self.compiler.compile_file(p): console.print("[green]✓ Clean[/green]")
        obs = Observer()
        obs.schedule(Handler(on_change), str(SRC_DIR), recursive=True)
        obs.start()
        try:
            while True: time.sleep(0.5)
        except KeyboardInterrupt:
            obs.stop()
        obs.join(); return True

    def _report(self) -> bool:
        n = self.db.execute("SELECT COUNT(*) FROM fixes").fetchone()[0]
        console.print(f"[cyan]Report:[/cyan] {n} fix events recorded")
        rows = self.db.execute("SELECT * FROM fixes ORDER BY ts DESC LIMIT 10").fetchall()
        for r in rows:
            console.print(f"{r[1]} - {r[2]}: {r[3]} ({r[4]:.2f}, success={r[5]})")
        return True

    def _handle(self, e: CompilationError) -> bool:
        rel = e.file_path.relative_to(ROOT) if e.file_path.exists() else e.file_path
        console.print(f"\n[bold red]Error[/bold red] {rel}:{e.line}:{e.column}\n[yellow]{e.message}[/yellow]")
        if e.file_path.exists():
            lines = read_file_lines(e.file_path)
            a = max(1, e.line-3); b = min(len(lines), e.line+3)  # expanded snippet
            snippet = "\n".join(f"{i:4}: {lines[i-1]}" for i in range(a, b+1))
            console.print(Panel(snippet, title=str(rel), border_style="red"))
        fx = self._find_fix(e)
        if fx:
            console.print(f"[green]Suggested:[/green] {fx.description} ({int(fx.confidence*100)}%)")
            for (ln, old, new) in fx.changes:
                if old: console.print(f"[red]-  {old}[/red]")
                if new: console.print(f"[green]+ {new}[/green]")
            choice = Prompt.ask("Apply?", choices=["y","n","skip","quit"], default="y")
            if choice == "y":
                ok = self._apply(fx); console.print("[green]✓ Applied[/green]" if ok else "[red]✗ Failed[/red]")
                return True
            if choice == "skip": return True
            if choice == "quit": return False
        else:
            console.print("[yellow]No automatic fix available[/yellow]")
            if not Confirm.ask("Continue?"): return False
        return True

    def _find_fix(self, e: CompilationError) -> Optional[Fix]:
        if not e.file_path.exists(): return None
        content = read_file_lines(e.file_path)
        for f in self.fixers:
            if f.can_fix(e):
                fx = f.generate(e, content)
                if fx: return fx
        return None

    def _apply(self, fx: Fix) -> bool:
        p = fx.error.file_path
        if not p.exists(): return False
        backup = backup_file(p)
        try:
            lines = read_file_lines(p)
            # Sort changes descending to avoid index shifts on inserts
            for (ln, old, new) in sorted(fx.changes, key=lambda x: -x[0]):
                if ln <= 0:
                    lines.insert(0, new)
                elif ln > len(lines):
                    lines.append(new)
                else:
                    if lines[ln-1].strip() != old.strip(): return False  # safety: if changed meanwhile
                    lines[ln-1] = new
            write_file_lines(p, lines)
            # Recompile only the file to check if fixed
            post_errs = self.compiler.compile_file(p)
            ok = not any(self._same_error(e1, fx.error) for e1 in post_errs)
            self.db.execute("INSERT INTO fixes(file,message,fix_type,confidence,success) VALUES(?,?,?,?,?)",
                (str(p), fx.error.message, fx.fix_type, fx.confidence, 1 if ok else 0))
            self.db.commit()
            if not ok:
                shutil.copy2(backup, p)
                console.print("[yellow]Fix applied but error persists—reverted[/yellow]")
            return ok
        except Exception as ex:
            shutil.copy2(backup, p); console.print(f"[red]Exception applying fix:[/red] {ex}")
            return False

    def _same_error(self, a: CompilationError, b: CompilationError) -> bool:
        return a.file_path == b.file_path and a.line == b.line and a.message == b.message

    def _print_errors(self, errors: List[CompilationError]):
        if not errors: return
        grouped: Dict[Path, List[CompilationError]] = {}
        for e in errors: grouped.setdefault(e.file_path, []).append(e)
        for fp, es in grouped.items():
            rel = fp.relative_to(ROOT) if fp.exists() else fp
            console.print(f"\n[bold]File:[/bold] {rel}")
            for e in es:
                console.print(f"  L{e.line:4}:{e.column:2} - {e.message}")

def main():
    parser = argparse.ArgumentParser(description="Doctor Chimera")
    sub = parser.add_subparsers(dest="cmd", required=True)
    p_java = sub.add_parser("java", help="Diagnose/Fix Java")
    p_java.add_argument("mode", choices=["diagnose","interactive","fix","watch","report"])
    args = parser.parse_args()
    if args.cmd == "java":
        ok = Doctor(args.mode).run()
        sys.exit(0 if ok else 1)

if __name__ == "__main__":
    main()
