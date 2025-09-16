#!/usr/bin/env python3
"""
ultimate_file_selector.py - Improved file selector using ttk.Treeview with
large, clickable Unicode checkbox glyphs for reliable cross-platform behavior.

This version intentionally avoids runtime image generation/PPM data to prevent
Tkinter image format issues and instead uses larger fonts + checkbox glyphs
for crisp, accessible checkboxes.
"""

import os
import sys
import asyncio
import logging
from pathlib import Path
from datetime import datetime
from typing import List, Dict, Set
from dataclasses import dataclass
from collections import defaultdict

import tkinter as tk
from tkinter import ttk, messagebox
import tkinter.font as tkfont

# ---------------------------------------------------------------------------
# Logging
# ---------------------------------------------------------------------------
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger(__name__)


# ---------------------------------------------------------------------------
# Data classes
# ---------------------------------------------------------------------------
@dataclass
class FileInfo:
    """Simple container for file info."""
    path: Path
    size: int
    modified: datetime

    @classmethod
    def from_path(cls, p: Path) -> "FileInfo":
        stat = p.stat()
        return cls(path=p, size=stat.st_size, modified=datetime.fromtimestamp(stat.st_mtime))


@dataclass
class SummaryConfig:
    mode: str = "detailed"
    include_git: bool = False
    truncate_kb: int = 5
    auto_open: bool = True


# ---------------------------------------------------------------------------
# File processor (async file opening)
# ---------------------------------------------------------------------------
class FileProcessor:
    """Perform platform-appropriate file opening operations."""

    @staticmethod
    async def open_in_notepad(file_path: Path) -> bool:
        """Try to open a file using notepad on Windows/WSL or xdg-open on Linux."""
        try:
            if sys.platform != "win32":
                # Try WSL to Windows conversion, else xdg-open fallback.
                try:
                    proc = await asyncio.create_subprocess_exec(
                        "wslpath", "-w", str(file_path),
                        stdout=asyncio.subprocess.PIPE,
                        stderr=asyncio.subprocess.PIPE
                    )
                    stdout, stderr = await proc.communicate()
                    if proc.returncode == 0:
                        win_path = stdout.decode().strip()
                        await asyncio.create_subprocess_exec(
                            "notepad.exe", win_path,
                            stdout=asyncio.subprocess.DEVNULL,
                            stderr=asyncio.subprocess.DEVNULL
                        )
                    else:
                        await asyncio.create_subprocess_exec(
                            "xdg-open", str(file_path),
                            stdout=asyncio.subprocess.DEVNULL,
                            stderr=asyncio.subprocess.DEVNULL
                        )
                except FileNotFoundError:
                    # wslpath not found -> fallback to system open
                    os.system(f'xdg-open "{file_path}" 2>/dev/null &')
            else:
                await asyncio.create_subprocess_exec(
                    "notepad.exe", str(file_path),
                    stdout=asyncio.subprocess.DEVNULL,
                    stderr=asyncio.subprocess.DEVNULL
                )
            return True
        except Exception as e:
            logger.error("Failed to open %s: %s", file_path, e)
            return False

    @staticmethod
    async def open_files_batch(files: List[Path]) -> Dict[str, int]:
        tasks = [FileProcessor.open_in_notepad(f) for f in files]
        results = await asyncio.gather(*tasks, return_exceptions=True)
        success = sum(1 for r in results if r is True)
        failed = len(results) - success
        return {"success": success, "failed": failed}


# ---------------------------------------------------------------------------
# Summary generator (kept mostly as-is)
# ---------------------------------------------------------------------------
class SummaryGenerator:
    def __init__(self, config: SummaryConfig):
        self.config = config

    async def generate(self, files: List[Path], directory: Path) -> str:
        if self.config.mode == "basic":
            return await self._generate_basic(files, directory)
        if self.config.mode == "config_query":
            return await self._generate_config_query(files, directory)
        return await self._generate_detailed(files, directory)

    async def _generate_basic(self, files: List[Path], directory: Path) -> str:
        lines = [
            "# FILE SELECTION SUMMARY",
            "",
            f"**Generated:** {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}",
            f"**Directory:** `{directory}`",
            f"**Total Files:** {len(files)}",
            "",
            "## Files",
            ""
        ]
        for f in sorted(files):
            try:
                lines.append(f"- `{f.name}` ({self._format_size(f.stat().st_size)})")
            except Exception:
                lines.append(f"- `{f.name}` (unknown size)")
        return "\n".join(lines)

    async def _generate_detailed(self, files: List[Path], directory: Path) -> str:
        lines = [
            "# DETAILED FILE SELECTION SUMMARY",
            "",
            f"**Generated:** {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}",
            f"**Directory:** `{directory}`",
            f"**Total Files:** {len(files)}",
            "",
            "---",
            ""
        ]
        by_ext = defaultdict(list)
        total_size = 0
        for f in files:
            by_ext[f.suffix or ".no_ext"].append(f)
            try:
                total_size += f.stat().st_size
            except Exception:
                pass

        lines.append("## Files by Type\n")
        for ext, ext_files in sorted(by_ext.items()):
            lines.append(f"### {ext} ({len(ext_files)} files)\n")
            for ef in ext_files[:10]:
                try:
                    lines.append(f"- `{ef.name}` ({self._format_size(ef.stat().st_size)})")
                except Exception:
                    lines.append(f"- `{ef.name}`")
            if len(ext_files) > 10:
                lines.append(f"- ... and {len(ext_files) - 10} more")
            lines.append("")

        lines.append("---\n")
        lines.append("## File Previews\n")
        truncate_size = self.config.truncate_kb * 1024
        preview_count = min(10, len(files))

        for f in files[:preview_count]:
            lines.append(f"### 📄 {f.name}\n")
            try:
                size = f.stat().st_size
                lines.append(f"**Size:** {self._format_size(size)}\n")
                content = f.read_text(encoding="utf-8", errors="ignore")
                if truncate_size > 0 and len(content) > truncate_size:
                    content = content[:truncate_size]
                    truncated = True
                else:
                    truncated = False
                lang = self._get_language(f.suffix)
                lines.append(f"```{lang}")
                lines.append(content)
                if truncated:
                    lines.append(f"... (truncated to {self.config.truncate_kb}KB)")
                lines.append("```")
                lines.append("")
            except Exception as e:
                lines.append(f"Error reading file: {e}\n")

        if len(files) > preview_count:
            lines.append(f"*Showing first {preview_count} files only*")

        lines.extend([
            "",
            "---",
            "",
            "## Statistics",
            "",
            f"- **Total Files:** {len(files)}",
            f"- **Total Size:** {self._format_size(total_size)}",
            f"- **File Types:** {len(by_ext)}"
        ])
        if files:
            lines.append(f"- **Average Size:** {self._format_size(total_size // len(files))}")
        return "\n".join(lines)

    async def _generate_config_query(self, files: List[Path], directory: Path) -> str:
        lines = [
            "# CONFIGURATION QUERY OUTPUT",
            f"Generated: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}",
            f"Directory: {directory}",
            "=" * 80,
            ""
        ]
        truncate_size = self.config.truncate_kb * 1024
        for f in files:
            lines.append("-" * 60)
            lines.append(f"File: {f.name}")
            try:
                stat = f.stat()
                lines.append(f"Size: {stat.st_size} bytes")
                content = f.read_text(encoding="utf-8", errors="ignore")
                if truncate_size > 0 and len(content) > truncate_size:
                    content = content[:truncate_size] + "\n*(truncated)*"
                lang = self._get_language(f.suffix)
                lines.append(f"```{lang}")
                lines.append(content)
                lines.append("```")
            except Exception as e:
                lines.append(f"Error: {e}")
            lines.append("")
        lines.append("=" * 80)
        lines.append(f"Total: {len(files)} files")
        return "\n".join(lines)

    def _format_size(self, size: int) -> str:
        for unit in ["B", "KB", "MB", "GB"]:
            if size < 1024.0:
                return f"{size:.1f} {unit}"
            size /= 1024.0
        return f"{size:.1f} TB"

    def _get_language(self, suffix: str) -> str:
        mapping = {
            ".py": "python", ".java": "java", ".xml": "xml", ".js": "javascript",
            ".ts": "typescript", ".cpp": "cpp", ".c": "c", ".cs": "csharp",
            ".go": "go", ".rs": "rust", ".sql": "sql", ".json": "json",
            ".yaml": "yaml", ".yml": "yaml", ".html": "html", ".css": "css",
            ".sh": "bash", ".md": "markdown", ".txt": "text"
        }
        return mapping.get(suffix.lower(), "")


# ---------------------------------------------------------------------------
# UI: Treeview with Unicode checkbox glyphs
# ---------------------------------------------------------------------------
class FileSelectorGUI:
    CHECK_OFF = "☐"  # roomy empty box
    CHECK_ON = "☑"   # checked box glyph

    def __init__(self, directory: Path):
        self.directory = directory.resolve()

        # model/state
        self.file_infos: Dict[Path, FileInfo] = {}
        self.all_files: List[Path] = []
        self.checked_files: Set[Path] = set()
        self.path_to_item: Dict[Path, str] = {}
        self.item_to_path: Dict[str, Path] = {}

        self.summary_config = SummaryConfig()

        # root window and styling
        self.root = tk.Tk()
        self.root.title(f"⚡ Ultimate File Selector - {self.directory}")
        self.root.geometry("1100x750")
        self.style = ttk.Style(self.root)
        try:
            self.style.theme_use("clam")
        except Exception:
            pass

        # fonts and spacing
        # larger font so checkbox glyphs are large and accessible
        self.tree_font = tkfont.Font(family="Consolas", size=12)
        self.heading_font = tkfont.Font(family="Arial", size=12, weight="bold")
        self.row_height = 34  # taller rows for easier clicking
        self._configure_style()

        # build UI and load files
        self._setup_gui()
        self._load_files()

    def _configure_style(self):
        """Configure Treeview sizing and fonts."""
        self.style.configure("Treeview", font=self.tree_font, rowheight=self.row_height)
        self.style.configure("Treeview.Heading", font=self.heading_font)
        self.style.map("Treeview", background=[("selected", "#cde6ff")])

    def _setup_gui(self):
        """Create and arrange widgets."""
        # header
        header = tk.Frame(self.root, bg="#2c3e50", height=70)
        header.pack(fill=tk.X)
        tk.Label(header, text="⚡ Ultimate File Selector",
                 font=("Arial", 16, "bold"), fg="white", bg="#2c3e50").pack(pady=(10, 0))
        tk.Label(header, text=f"📁 {self.directory}", font=("Consolas", 10),
                 fg="#ecf0f1", bg="#2c3e50").pack()

        # progress bar
        self.progress_var = tk.DoubleVar()
        self.progress_bar = ttk.Progressbar(self.root, variable=self.progress_var, maximum=100)
        self.progress_bar.pack(fill=tk.X, padx=10, pady=6)

        # controls
        control_frame = tk.Frame(self.root)
        control_frame.pack(fill=tk.X, padx=10, pady=6)
        tk.Label(control_frame, text="🔍", font=("Arial", 14)).pack(side=tk.LEFT)
        self.search_var = tk.StringVar()
        self.search_var.trace_add("write", lambda *_: self._refresh_display())
        self.search_entry = ttk.Entry(control_frame, textvariable=self.search_var, font=("Consolas", 11))
        self.search_entry.pack(side=tk.LEFT, fill=tk.X, expand=True, padx=6)
        ttk.Button(control_frame, text="Clear", command=lambda: self.search_var.set("")).pack(side=tk.LEFT, padx=2)

        right_buttons = tk.Frame(control_frame)
        right_buttons.pack(side=tk.RIGHT)
        ttk.Button(right_buttons, text="✅ Check All", command=self._select_all).pack(side=tk.LEFT, padx=3)
        ttk.Button(right_buttons, text="❌ Uncheck All", command=self._select_none).pack(side=tk.LEFT, padx=3)
        ttk.Button(right_buttons, text="🔄 Invert", command=self._invert_selection).pack(side=tk.LEFT, padx=3)

        tk.Label(control_frame, text="Filter:").pack(side=tk.RIGHT, padx=(10, 4))
        self.filter_var = tk.StringVar(value="All")
        self.filter_combo = ttk.Combobox(control_frame, textvariable=self.filter_var, values=["All"],
                                         width=14, state="readonly")
        self.filter_combo.pack(side=tk.RIGHT, padx=4)
        self.filter_combo.bind("<<ComboboxSelected>>", lambda e: self._refresh_display())

        # treeview list
        list_frame = tk.Frame(self.root)
        list_frame.pack(fill=tk.BOTH, expand=True, padx=10, pady=6)
        vsb = ttk.Scrollbar(list_frame, orient="vertical")
        hsb = ttk.Scrollbar(list_frame, orient="horizontal")

        self.tree = ttk.Treeview(list_frame, columns=("size", "modified", "ext"),
                                 show="tree headings", yscrollcommand=vsb.set, xscrollcommand=hsb.set)
        vsb.config(command=self.tree.yview)
        hsb.config(command=self.tree.xview)
        self.tree.grid(row=0, column=0, sticky="nsew")
        vsb.grid(row=0, column=1, sticky="ns")
        hsb.grid(row=1, column=0, sticky="ew")
        list_frame.grid_rowconfigure(0, weight=1)
        list_frame.grid_columnconfigure(0, weight=1)

        # configure columns
        self.tree.heading("#0", text="Name", anchor="w")
        self.tree.column("#0", anchor="w", width=520, minwidth=180)
        self.tree.heading("size", text="Size", anchor="e")
        self.tree.column("size", anchor="e", width=120, minwidth=80)
        self.tree.heading("modified", text="Modified", anchor="center")
        self.tree.column("modified", anchor="center", width=180, minwidth=120)
        self.tree.heading("ext", text="Ext", anchor="center")
        self.tree.column("ext", anchor="center", width=80, minwidth=50)

        # checked row tag
        self.tree.tag_configure("checked", background="#ecffef")

        # events
        self.tree.bind("<Button-1>", self._on_tree_click, add="+")
        self.tree.bind("<Double-1>", self._on_tree_double_click, add="+")

        # summary options
        options_frame = tk.LabelFrame(self.root, text="📊 Summary Options")
        options_frame.pack(fill=tk.X, padx=10, pady=6)

        self.generate_summary_var = tk.BooleanVar(value=False)
        tk.Checkbutton(options_frame, text="Generate summary file",
                       variable=self.generate_summary_var,
                       command=self._toggle_summary_options).grid(row=0, column=0, sticky="w", padx=6, pady=2)

        self.auto_open_var = tk.BooleanVar(value=True)
        self.auto_open_check = tk.Checkbutton(options_frame, text="Auto-open summary",
                                              variable=self.auto_open_var, state=tk.DISABLED)
        self.auto_open_check.grid(row=0, column=1, sticky="w", padx=6, pady=2)

        tk.Label(options_frame, text="Mode:").grid(row=1, column=0, sticky="w", padx=6)
        self.mode_var = tk.StringVar(value="detailed")
        self.mode_combo = ttk.Combobox(options_frame, textvariable=self.mode_var,
                                       values=["basic", "detailed", "config_query"], width=15, state="disabled")
        self.mode_combo.grid(row=1, column=1, sticky="w", padx=6, pady=2)

        tk.Label(options_frame, text="Truncate (KB):").grid(row=1, column=2, sticky="w", padx=6)
        self.truncate_var = tk.IntVar(value=5)
        self.truncate_spin = tk.Spinbox(options_frame, from_=0, to=100, textvariable=self.truncate_var,
                                        width=8, state="disabled")
        self.truncate_spin.grid(row=1, column=3, sticky="w", padx=6, pady=2)

        # status bar
        self.status_frame = tk.Frame(self.root, bg="#34495e", height=32)
        self.status_frame.pack(fill=tk.X)
        self.status_label = tk.Label(self.status_frame, text="Ready", fg="white", bg="#34495e",
                                     font=("Consolas", 10))
        self.status_label.pack(side=tk.LEFT, padx=10)
        self.selection_label = tk.Label(self.status_frame, text="0 files selected", fg="#3498db",
                                        bg="#34495e", font=("Consolas", 10, "bold"))
        self.selection_label.pack(side=tk.RIGHT, padx=10)

        # action buttons
        button_frame = tk.Frame(self.root)
        button_frame.pack(fill=tk.X, padx=10, pady=10)
        self.process_btn = tk.Button(button_frame, text="⚡ Process Selected", command=self._process_files,
                                     bg="#27ae60", fg="white", font=("Arial", 12, "bold"), height=2, width=20)
        self.process_btn.pack(side=tk.LEFT, padx=5)
        tk.Button(button_frame, text="❌ Cancel", command=self.root.quit,
                  bg="#e74c3c", fg="white", font=("Arial", 12, "bold"), height=2, width=20).pack(side=tk.RIGHT, padx=5)

        # shortcuts
        self.root.bind("<Control-a>", lambda e: self._select_all())
        self.root.bind("<Control-d>", lambda e: self._select_none())
        self.root.bind("<Control-i>", lambda e: self._invert_selection())
        self.root.bind("<Return>", lambda e: self._process_files())
        self.root.bind("<Escape>", lambda e: self.root.quit())

        # periodic update
        self.root.after(150, self._update_selection_count)

    def _toggle_summary_options(self):
        state = tk.NORMAL if self.generate_summary_var.get() else tk.DISABLED
        self.auto_open_check.config(state=state)
        self.mode_combo.config(state="readonly" if self.generate_summary_var.get() else "disabled")
        self.truncate_spin.config(state=state)

    def _load_files(self):
        """Scan directory and populate the model + tree."""
        self.status_label.config(text="Loading files...")
        self.progress_var.set(0)
        try:
            files = sorted([f for f in self.directory.iterdir() if f.is_file() and not f.name.startswith(".")])
            self.all_files = files
            self.file_infos.clear()
            self.tree.delete(*self.tree.get_children())
            extensions = set()

            total = max(1, len(files))
            for i, f in enumerate(files):
                try:
                    self.file_infos[f] = FileInfo.from_path(f)
                    extensions.add(f.suffix or "no ext")
                except Exception as e:
                    logger.error("Error loading %s: %s", f, e)
                self.progress_var.set((i + 1) / total * 100)
                self.root.update_idletasks()

            filter_values = ["All"] + sorted(extensions)
            self.filter_combo["values"] = filter_values
            self.filter_combo.set("All")

            self._refresh_display()
            self.status_label.config(text=f"Loaded {len(files)} files")
        except Exception as e:
            messagebox.showerror("Error", f"Failed to load files: {e}")
            self.status_label.config(text="Error loading files")

    # Helper: prepare the display name with checkbox glyph
    def _display_name(self, p: Path) -> str:
        glyph = self.CHECK_ON if p in self.checked_files else self.CHECK_OFF
        return f"{glyph} {p.name}"

    def _refresh_display(self):
        """Refresh Treeview items according to current filter/search, preserve check state."""
        try:
            first_visible = self.tree.get_children()[0] if self.tree.get_children() else None
        except Exception:
            first_visible = None

        self.tree.delete(*self.tree.get_children())
        search_term = self.search_var.get().lower().strip()
        filter_ext = self.filter_var.get()

        for p in self.all_files:
            info = self.file_infos.get(p)
            if not info:
                continue
            name_lower = p.name.lower()
            ext = p.suffix or "no ext"
            if search_term and search_term not in name_lower:
                continue
            if filter_ext != "All" and ext != filter_ext:
                continue

            display = self._display_name(p)
            size_str = self._format_size(info.size)
            date_str = info.modified.strftime("%Y-%m-%d %H:%M")
            item_id = str(p)
            self.path_to_item[p] = item_id
            self.item_to_path[item_id] = p
            if p in self.checked_files:
                self.tree.insert("", "end", iid=item_id, text=display, values=(size_str, date_str, p.suffix or ""),
                                 tags=("checked",))
            else:
                self.tree.insert("", "end", iid=item_id, text=display, values=(size_str, date_str, p.suffix or ""))

        if first_visible:
            try:
                self.tree.see(first_visible)
            except Exception:
                pass

    def _on_tree_click(self, event):
        """Toggle checkbox when clicking a row. Clicking whitespace does nothing."""
        item = self.tree.identify_row(event.y)
        if not item:
            return
        path = self.item_to_path.get(item)
        if not path:
            return
        if path in self.checked_files:
            self.checked_files.discard(path)
            # update text & tags
            self.tree.item(item, text=self._display_name(path), tags=())
        else:
            self.checked_files.add(path)
            self.tree.item(item, text=self._display_name(path), tags=("checked",))
        self._update_selection_count()

    def _on_tree_double_click(self, event):
        """Open file on double click (async)."""
        item = self.tree.identify_row(event.y)
        if not item:
            return
        path = self.item_to_path.get(item)
        if not path:
            return

        async def open_one():
            await FileProcessor.open_in_notepad(path)

        try:
            asyncio.run(open_one())
        except Exception as e:
            logger.error("Error opening file: %s", e)
            messagebox.showerror("Error", f"Failed to open {path.name}: {e}")

    def _select_all(self):
        """Select all visible items."""
        for iid in self.tree.get_children():
            path = self.item_to_path.get(iid)
            if path:
                self.checked_files.add(path)
                self.tree.item(iid, text=self._display_name(path), tags=("checked",))
        self._update_selection_count()

    def _select_none(self):
        """Deselect all."""
        self.checked_files.clear()
        for iid in self.tree.get_children():
            self.tree.item(iid, text=self._display_name(self.item_to_path.get(iid)), tags=())
        self._update_selection_count()

    def _invert_selection(self):
        """Invert selection for visible items."""
        for iid in self.tree.get_children():
            path = self.item_to_path.get(iid)
            if not path:
                continue
            if path in self.checked_files:
                self.checked_files.discard(path)
                self.tree.item(iid, text=self._display_name(path), tags=())
            else:
                self.checked_files.add(path)
                self.tree.item(iid, text=self._display_name(path), tags=("checked",))
        self._update_selection_count()

    def _update_selection_count(self):
        """Refresh selection counter in status."""
        self.selection_label.config(text=f"{len(self.checked_files)} files selected")
        # heartbeat (lightweight)
        self.root.after(200, lambda: None)

    def _format_size(self, size: int) -> str:
        for unit in ["B", "KB", "MB", "GB"]:
            if size < 1024.0:
                return f"{size:.1f} {unit}"
            size /= 1024.0
        return f"{size:.1f} TB"

    def _process_files(self):
        if not self.checked_files:
            messagebox.showwarning("No Selection", "Please select files to process")
            return
        selected_files = list(self.checked_files)
        self.process_btn.config(state=tk.DISABLED)
        self.status_label.config(text=f"Processing {len(selected_files)} files...")
        self.root.after(100, lambda: self._run_async_processing(selected_files))

    def _run_async_processing(self, files: List[Path]):
        async def process():
            try:
                processor = FileProcessor()
                results = await processor.open_files_batch(files)
                summary_file = None
                if self.generate_summary_var.get():
                    self.status_label.config(text="Generating summary...")
                    self.summary_config.mode = self.mode_var.get()
                    self.summary_config.truncate_kb = self.truncate_var.get()
                    self.summary_config.auto_open = self.auto_open_var.get()
                    generator = SummaryGenerator(self.summary_config)
                    content = await generator.generate(files, self.directory)
                    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
                    summary_file = self.directory / f"file_summary_{timestamp}.md"
                    summary_file.write_text(content, encoding="utf-8")
                    if self.summary_config.auto_open:
                        await processor.open_in_notepad(summary_file)

                msg = f"✅ Success: {results['success']} | ❌ Failed: {results['failed']}"
                if summary_file:
                    msg += f"\n\n📊 Summary saved to:\n{summary_file.name}"
                self.status_label.config(text=msg)
                if results["failed"] == 0:
                    messagebox.showinfo("Success", msg)
                else:
                    messagebox.showwarning("Partial Success", msg)
            except Exception as e:
                logger.exception("Processing error")
                messagebox.showerror("Error", f"Processing failed: {e}")
                self.status_label.config(text="Error processing files")
            finally:
                self.process_btn.config(state=tk.NORMAL)

        try:
            asyncio.run(process())
        except Exception as e:
            logger.exception("Async processing error")
            messagebox.showerror("Error", f"Failed to process: {e}")
            self.process_btn.config(state=tk.NORMAL)

    def run(self):
        self.root.mainloop()


# ---------------------------------------------------------------------------
# Main entrypoint
# ---------------------------------------------------------------------------
def main():
    import argparse
    parser = argparse.ArgumentParser(description="Ultimate file selector with improved UI")
    parser.add_argument("path", nargs="?", default=".", help="Directory path (default: current)")
    args = parser.parse_args()
    directory = Path(args.path).resolve()
    if not directory.exists():
        print(f"❌ Directory '{directory}' does not exist")
        sys.exit(1)
    if not directory.is_dir():
        print(f"❌ '{directory}' is not a directory")
        sys.exit(1)
    try:
        app = FileSelectorGUI(directory)
        app.run()
    except Exception as e:
        print(f"❌ Fatal error: {e}")
        logger.exception("Fatal error")
        sys.exit(1)


if __name__ == "__main__":
    main()
