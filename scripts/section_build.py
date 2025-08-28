#!/usr/bin/env python3
"""
OrphanageHub Section Compiler v5.0 (Definitive Edition)
- Auto-discovers sections for zero-maintenance
- Compiles in dependency order with correct downstream invalidation
- Provides timing diagnostics and rich dashboard
- 100% cross-platform and CI/CD ready
"""

from __future__ import annotations
import argparse
import os
import re
import shutil
import subprocess
import sys
import time
import hashlib
import threading
from pathlib import Path
from typing import Dict, List, Tuple

# --- Configuration ---
ROOT = Path(__file__).resolve().parent.parent
SRC_JAVA = ROOT / "src" / "main" / "java"
SRC_RESOURCES = ROOT / "src" / "main" / "resources"
TARGET = ROOT / "target"
SECTION_CLASSES = TARGET / "section-classes"
JAVA_RELEASE = "17"

# --- Rich output with graceful fallback ---
try:
    from rich.console import Console
    from rich.table import Table
    from rich import box
    console = Console(highlight=False)
    RICH_AVAILABLE = True
except ImportError:
    class DummyConsole:
        def print(self, text, *args, **kwargs): 
            print(text)
    console = DummyConsole()
    RICH_AVAILABLE = False

# --- Auto-discovery of Sections ---
SECTION_ORDER = ["util", "model", "dao", "service", "tools", "gui"]
PKG_ROOT = SRC_JAVA / "com" / "orphanagehub"

def discover_sections():
    """Discover all sections maintaining dependency order"""
    if not PKG_ROOT.exists():
        return []
    
    found_pkgs = {p.name for p in PKG_ROOT.iterdir() if p.is_dir() and not p.name.startswith('.')}
    sections = []
    
    # Add known sections in order
    for name in SECTION_ORDER:
        if name in found_pkgs:
            sections.append((name, [PKG_ROOT / name]))
    
    # Add any new sections alphabetically
    for name in sorted(found_pkgs - set(SECTION_ORDER)):
        sections.append((name, [PKG_ROOT / name]))
    
    return sections

SECTIONS = discover_sections()

# --- Core Build Logic ---

def get_maven_cmd():
    """Get platform-specific Maven wrapper command"""
    return "mvnw.cmd" if os.name == "nt" else "./mvnw"

def get_ext_classpath() -> str:
    """Use Maven to get the project's dependency classpath, caching the result."""
    cp_file = TARGET / "ext-cp.txt"
    pom_file = ROOT / "pom.xml"
    
    # Check if cache is valid
    if cp_file.exists() and pom_file.exists():
        if cp_file.stat().st_mtime > pom_file.stat().st_mtime:
            return cp_file.read_text().strip()
    
    console.print("[dim]Updating Maven classpath...[/dim]")
    cmd = [get_maven_cmd(), "-q", "-B", "dependency:build-classpath", 
           f"-Dmdep.outputFile={cp_file}"]
    
    try:
        result = subprocess.run(cmd, check=True, cwd=ROOT, capture_output=True, text=True)
        if cp_file.exists():
            return cp_file.read_text().strip()
    except subprocess.CalledProcessError:
        console.print("[yellow]Warning: Could not get Maven classpath[/yellow]")
    
    return ""

def get_section_hash(files: List[Path]) -> str:
    """Calculate hash for a list of files"""
    hasher = hashlib.sha256()
    for file in sorted(files):
        if file.exists():
            hasher.update(file.read_bytes())
    return hasher.hexdigest()

def compile_section(name: str, dirs: List[Path], cp: str, force: bool = False) -> Dict:
    """Compile a section with caching and stale-proofing"""
    outdir = SECTION_CLASSES / name
    cache_file = outdir / ".cachehash"
    
    # Find Java files
    java_files = []
    for d in dirs:
        if d.exists():
            java_files.extend(d.rglob("*.java"))
    java_files = sorted(java_files)
    
    if not java_files:
        return {"status": "skipped", "rebuilt": False, "time": 0, 
                "files": 0, "errors": 0, "warnings": 0}
    
    # Check cache
    current_hash = get_section_hash(java_files)
    if not force and cache_file.exists():
        try:
            if cache_file.read_text().strip() == current_hash:
                return {"status": "cached", "rebuilt": False, "time": 0,
                        "files": len(java_files), "errors": 0, "warnings": 0}
        except:
            pass
    
    # Clean output directory (stale-proofing)
    if outdir.exists():
        shutil.rmtree(outdir, ignore_errors=True)
    outdir.mkdir(parents=True, exist_ok=True)
    
    # Write sources list
    sources_file = outdir / "sources.list"
    sources_file.write_text("\n".join(str(f) for f in java_files))
    
    # Compile
    start_time = time.perf_counter()
    cmd = ["javac", "--release", JAVA_RELEASE, "-Xlint:all", "-g",
           "-d", str(outdir), "-cp", cp, f"@{sources_file}"]
    
    result = subprocess.run(cmd, capture_output=True, text=True, cwd=ROOT)
    elapsed = time.perf_counter() - start_time
    
    # Parse output
    errors = 0
    warnings = 0
    diags = []
    
    for line in result.stderr.splitlines():
        if ": error:" in line:
            errors += 1
            diags.append({"type": "error", "msg": line})
        elif ": warning:" in line:
            warnings += 1
            diags.append({"type": "warning", "msg": line})
    
    # Update cache on success
    if result.returncode == 0:
        cache_file.write_text(current_hash)
    
    return {
        "status": "ok" if result.returncode == 0 else "failed",
        "rebuilt": True,
        "time": elapsed,
        "files": len(java_files),
        "errors": errors,
        "warnings": warnings,
        "diags": diags
    }

def build_all_sections() -> Tuple[Dict, List[str]]:
    """Compile all sections in dependency order"""
    results = {}
    ext_classpath = get_ext_classpath()
    current_cp_parts = [ext_classpath] if ext_classpath else []
    upstream_changed = False
    
    for name, dirs in SECTIONS:
        # Build classpath
        cp_str = os.pathsep.join(filter(None, current_cp_parts))
        
        # Compile with force if upstream changed
        result = compile_section(name, dirs, cp_str, force=upstream_changed)
        results[name] = result
        
        # Track if this section was rebuilt
        if result.get("rebuilt"):
            upstream_changed = True
        
        # Add to classpath if compiled
        if result.get("status") != "skipped":
            current_cp_parts.append(str(SECTION_CLASSES / name))
        
        # Stop on failure
        if result.get("status") == "failed":
            break
    
    # Copy resources if all succeeded
    if all(r.get("status") != "failed" for r in results.values()):
        resource_outdir = SECTION_CLASSES / "resources"
        if resource_outdir.exists():
            shutil.rmtree(resource_outdir, ignore_errors=True)
        if SRC_RESOURCES.is_dir():
            shutil.copytree(SRC_RESOURCES, resource_outdir, dirs_exist_ok=True)
            current_cp_parts.append(str(resource_outdir))
    
    return results, current_cp_parts

def print_summary(results: Dict) -> int:
    """Print compilation summary and return exit code"""
    if not RICH_AVAILABLE:
        # Simple output
        total_errors = 0
        for name, result in results.items():
            status = result.get("status", "unknown")
            errors = result.get("errors", 0)
            total_errors += errors
            print(f"{name}: {status} ({errors} errors)")
        return 1 if total_errors > 0 else 0
    
    # Rich output
    total_errors = sum(r.get("errors", 0) for r in results.values())
    
    table = Table(
        box=box.ROUNDED,
        show_header=True,
        header_style="bold cyan",
        title="[bold]Build Summary[/bold]"
    )
    
    table.add_column("Section", style="bold", width=12)
    table.add_column("Status", justify="center")
    table.add_column("Files", justify="right")
    table.add_column("Errors", justify="right")
    table.add_column("Time", justify="right")
    
    for name, _ in SECTIONS:
        r = results.get(name, {})
        
        # Status display
        status = r.get("status", "pending")
        status_display = {
            "ok": "[green]✓ Built[/green]",
            "cached": "[dim]≡ Cached[/dim]",
            "failed": "[red]✗ Failed[/red]",
            "skipped": "[yellow]○ Skipped[/yellow]"
        }.get(status, status)
        
        # Error display
        errors = r.get("errors", 0)
        error_display = f"[red]{errors}[/red]" if errors > 0 else "0"
        
        # Time display
        time_val = r.get("time", 0)
        time_display = f"{time_val:.2f}s" if time_val > 0.01 else "-"
        
        table.add_row(
            name,
            status_display,
            str(r.get("files", 0)),
            error_display,
            time_display
        )
    
    console.print(table)
    
    # Show errors if any
    if total_errors > 0:
        console.print(f"\n[red]Build failed with {total_errors} error(s)[/red]")
        
        # Show first error
        for name, r in results.items():
            if r.get("errors", 0) > 0 and r.get("diags"):
                first_error = r["diags"][0]["msg"]
                console.print(f"\n[yellow]First error in {name}:[/yellow]")
                console.print(f"[dim]{first_error[:300]}[/dim]")
                break
    else:
        total_time = sum(r.get("time", 0) for r in results.values())
        console.print(f"\n[green]✓ Build successful ({total_time:.2f}s total)[/green]")
    
    return 1 if total_errors > 0 else 0

def watch_mode():
    """Watch for changes and auto-compile"""
    try:
        from watchdog.observers import Observer
        from watchdog.events import FileSystemEventHandler
    except ImportError:
        console.print("[red]Watch mode requires 'watchdog': pip install watchdog[/red]")
        sys.exit(1)
    
    class ChangeHandler(FileSystemEventHandler):
        def __init__(self):
            self.timer = None
            self.lock = threading.Lock()
        
        def rebuild(self):
            with self.lock:
                self.timer = None
            
            os.system('cls' if os.name == 'nt' else 'clear')
            console.print(f"[yellow]Rebuilding... {time.strftime('%H:%M:%S')}[/yellow]\n")
            
            results, _ = build_all_sections()
            print_summary(results)
            console.print("\n[dim]Watching for changes... (Ctrl+C to stop)[/dim]")
        
        def on_any_event(self, event):
            if event.is_directory:
                return
            
            if event.src_path.endswith(('.java', '.properties')):
                with self.lock:
                    if self.timer:
                        self.timer.cancel()
                    self.timer = threading.Timer(0.5, self.rebuild)
                    self.timer.start()
    
    # Initial build
    console.print("[cyan]Initial build...[/cyan]\n")
    results, _ = build_all_sections()
    print_summary(results)
    console.print("\n[dim]Watching for changes... (Ctrl+C to stop)[/dim]")
    
    # Start watching
    observer = Observer()
    handler = ChangeHandler()
    observer.schedule(handler, str(SRC_JAVA), recursive=True)
    if SRC_RESOURCES.exists():
        observer.schedule(handler, str(SRC_RESOURCES), recursive=True)
    observer.start()
    
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        observer.stop()
        console.print("\n[yellow]Stopping watcher...[/yellow]")
    observer.join()

def clean():
    """Clean all build artifacts"""
    if SECTION_CLASSES.exists():
        shutil.rmtree(SECTION_CLASSES)
    
    ext_cp = TARGET / "ext-cp.txt"
    if ext_cp.exists():
        ext_cp.unlink()
    
    console.print("[green]✓ Section build artifacts cleaned[/green]")

def main():
    parser = argparse.ArgumentParser(
        description="OrphanageHub Section Compiler",
        formatter_class=argparse.RawDescriptionHelpFormatter
    )
    
    parser.add_argument("--watch", action="store_true",
                       help="Watch mode - auto-compile on changes")
    parser.add_argument("--clean", action="store_true",
                       help="Clean all build artifacts")
    parser.add_argument("--get-classpath", action="store_true",
                       help="Output the full runtime classpath")
    parser.add_argument("--force", action="store_true",
                       help="Force rebuild all sections")
    
    args = parser.parse_args()
    
    if not SECTIONS:
        console.print(f"[red]Error: No sections found in {PKG_ROOT}[/red]")
        sys.exit(1)
    
    if args.clean:
        clean()
    elif args.get_classpath:
        _, cp_parts = build_all_sections()
        print(os.pathsep.join(cp_parts))
    elif args.watch:
        watch_mode()
    else:
        results, _ = build_all_sections()
        sys.exit(print_summary(results))

if __name__ == "__main__":
    main()
