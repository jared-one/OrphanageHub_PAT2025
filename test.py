#!/usr/bin/env python3
"""
ultimate_file_selector.py - Advanced file selector with checkboxes and filtering
Fully functional version with proper state management
"""

import os
import sys
import subprocess
import asyncio
import threading
from pathlib import Path
from datetime import datetime
from typing import List, Dict, Optional, Set, Any, Tuple
from dataclasses import dataclass
from collections import defaultdict
import tkinter as tk
from tkinter import ttk, messagebox
import tkinter.font as tkfont
import logging

# Setup logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# ==============================================================================
# DATA CLASSES
# ==============================================================================
@dataclass
class FileInfo:
    """File information container"""
    path: Path
    size: int
    modified: datetime
    
    @classmethod
    def from_path(cls, path: Path) -> 'FileInfo':
        """Create FileInfo from path"""
        stat = path.stat()
        return cls(
            path=path,
            size=stat.st_size,
            modified=datetime.fromtimestamp(stat.st_mtime)
        )

@dataclass
class SummaryConfig:
    """Configuration for summary generation"""
    mode: str = "detailed"
    include_git: bool = False
    truncate_kb: int = 5
    auto_open: bool = True

# ==============================================================================
# FILE PROCESSOR
# ==============================================================================
class FileProcessor:
    """Handles file operations"""
    
    @staticmethod
    async def open_in_notepad(file_path: Path) -> bool:
        """Open file in notepad (Windows/WSL)"""
        try:
            # Try to get Windows path for WSL
            if sys.platform != "win32":
                try:
                    proc = await asyncio.create_subprocess_exec(
                        'wslpath', '-w', str(file_path),
                        stdout=asyncio.subprocess.PIPE,
                        stderr=asyncio.subprocess.PIPE
                    )
                    stdout, stderr = await proc.communicate()
                    
                    if proc.returncode == 0:
                        win_path = stdout.decode().strip()
                        await asyncio.create_subprocess_exec(
                            'notepad.exe', win_path,
                            stdout=asyncio.subprocess.DEVNULL,
                            stderr=asyncio.subprocess.DEVNULL
                        )
                    else:
                        # Fallback to xdg-open on Linux
                        await asyncio.create_subprocess_exec(
                            'xdg-open', str(file_path),
                            stdout=asyncio.subprocess.DEVNULL,
                            stderr=asyncio.subprocess.DEVNULL
                        )
                except:
                    # Direct fallback
                    os.system(f'xdg-open "{file_path}" 2>/dev/null &')
            else:
                # Direct Windows
                await asyncio.create_subprocess_exec(
                    'notepad.exe', str(file_path),
                    stdout=asyncio.subprocess.DEVNULL,
                    stderr=asyncio.subprocess.DEVNULL
                )
            return True
        except Exception as e:
            logger.error(f"Failed to open {file_path}: {e}")
            return False
    
    @staticmethod
    async def open_files_batch(files: List[Path]) -> Dict[str, int]:
        """Open multiple files concurrently"""
        tasks = [FileProcessor.open_in_notepad(f) for f in files]
        results = await asyncio.gather(*tasks, return_exceptions=True)
        
        success = sum(1 for r in results if r is True)
        failed = len(results) - success
        
        return {'success': success, 'failed': failed}

# ==============================================================================
# SUMMARY GENERATOR
# ==============================================================================
class SummaryGenerator:
    """Generates file summaries in various formats"""
    
    def __init__(self, config: SummaryConfig):
        self.config = config
    
    async def generate(self, files: List[Path], directory: Path) -> str:
        """Generate summary based on configuration"""
        if self.config.mode == "basic":
            return await self._generate_basic(files, directory)
        elif self.config.mode == "config_query":
            return await self._generate_config_query(files, directory)
        else:
            return await self._generate_detailed(files, directory)
    
    async def _generate_basic(self, files: List[Path], directory: Path) -> str:
        """Generate basic file list"""
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
                size = f.stat().st_size
                lines.append(f"- `{f.name}` ({self._format_size(size)})")
            except:
                lines.append(f"- `{f.name}` (unknown size)")
        
        return "\n".join(lines)
    
    async def _generate_detailed(self, files: List[Path], directory: Path) -> str:
        """Generate detailed summary with previews"""
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
        
        # Group by extension
        by_ext = defaultdict(list)
        total_size = 0
        
        for f in files:
            by_ext[f.suffix or '.no_ext'].append(f)
            try:
                total_size += f.stat().st_size
            except:
                pass
        
        # File list by type
        lines.append("## Files by Type")
        lines.append("")
        
        for ext, ext_files in sorted(by_ext.items()):
            lines.append(f"### {ext} ({len(ext_files)} files)")
            lines.append("")
            for f in ext_files[:10]:
                try:
                    size = f.stat().st_size
                    lines.append(f"- `{f.name}` ({self._format_size(size)})")
                except:
                    lines.append(f"- `{f.name}`")
            if len(ext_files) > 10:
                lines.append(f"- ... and {len(ext_files) - 10} more")
            lines.append("")
        
        # File previews
        lines.append("---")
        lines.append("")
        lines.append("## File Previews")
        lines.append("")
        
        truncate_size = self.config.truncate_kb * 1024
        preview_count = min(10, len(files))
        
        for f in files[:preview_count]:
            lines.append(f"### 📄 {f.name}")
            lines.append("")
            
            try:
                size = f.stat().st_size
                lines.append(f"**Size:** {self._format_size(size)}")
                
                # Read content
                content = f.read_text(encoding='utf-8', errors='ignore')
                if truncate_size > 0 and len(content) > truncate_size:
                    content = content[:truncate_size]
                    truncated = True
                else:
                    truncated = False
                
                # Detect language for syntax highlighting
                lang = self._get_language(f.suffix)
                
                lines.append("")
                lines.append(f"```{lang}")
                lines.append(content)
                if truncated:
                    lines.append(f"... (truncated to {self.config.truncate_kb}KB)")
                lines.append("```")
                lines.append("")
                
            except Exception as e:
                lines.append(f"Error reading file: {e}")
                lines.append("")
        
        if len(files) > preview_count:
            lines.append(f"*Showing first {preview_count} files only*")
        
        # Statistics
        lines.append("---")
        lines.append("")
        lines.append("## Statistics")
        lines.append("")
        lines.append(f"- **Total Files:** {len(files)}")
        lines.append(f"- **Total Size:** {self._format_size(total_size)}")
        lines.append(f"- **File Types:** {len(by_ext)}")
        if files:
            lines.append(f"- **Average Size:** {self._format_size(total_size // len(files))}")
        
        return "\n".join(lines)
    
    async def _generate_config_query(self, files: List[Path], directory: Path) -> str:
        """Generate config_query.py style summary"""
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
                
                content = f.read_text(encoding='utf-8', errors='ignore')
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
        """Format file size"""
        for unit in ['B', 'KB', 'MB', 'GB']:
            if size < 1024.0:
                return f"{size:.1f} {unit}"
            size /= 1024.0
        return f"{size:.1f} TB"
    
    def _get_language(self, suffix: str) -> str:
        """Get language from file extension"""
        mapping = {
            '.py': 'python', '.java': 'java', '.xml': 'xml',
            '.js': 'javascript', '.ts': 'typescript',
            '.cpp': 'cpp', '.c': 'c', '.cs': 'csharp',
            '.go': 'go', '.rs': 'rust', '.sql': 'sql',
            '.json': 'json', '.yaml': 'yaml', '.yml': 'yaml',
            '.html': 'html', '.css': 'css', '.sh': 'bash',
            '.md': 'markdown', '.txt': 'text'
        }
        return mapping.get(suffix.lower(), '')

# ==============================================================================
# MAIN GUI APPLICATION WITH PROPER STATE MANAGEMENT
# ==============================================================================
class FileSelectorGUI:
    """Main GUI application with checkboxes and filtering"""
    
    def __init__(self, directory: Path):
        self.directory = directory
        
        # File management - store by path, not item ID
        self.file_infos: Dict[Path, FileInfo] = {}
        self.checked_files: Set[Path] = set()  # Store checked file paths
        self.item_to_path: Dict[str, Path] = {}  # Map item IDs to paths
        self.path_to_item: Dict[Path, str] = {}  # Map paths to item IDs
        
        self.summary_config = SummaryConfig()
        
        # Setup GUI
        self.root = tk.Tk()
        self.root.title(f"🚀 Ultimate File Selector - {directory}")
        self.root.geometry("1100x750")
        
        self.style = ttk.Style()
        self.style.theme_use('clam')
        
        # Setup checkbox images
        self._setup_checkbox_images()
        self._setup_gui()
        self._load_files()
    
    def _setup_checkbox_images(self):
        """Setup checkbox images using simple Unicode characters"""
        # Create simple checkbox images using text
        self.checkbox_font = tkfont.Font(family="Arial", size=12)
        
        # We'll use Unicode characters for checkboxes instead of images
        self.CHECK_ON = "☑"
        self.CHECK_OFF = "☐"
    
    def _setup_gui(self):
        """Setup GUI components"""
        
        # Header
        header = tk.Frame(self.root, bg='#2c3e50', height=60)
        header.pack(fill=tk.X)
        
        tk.Label(
            header,
            text="⚡ Ultimate File Selector",
            font=('Arial', 16, 'bold'),
            fg='white',
            bg='#2c3e50'
        ).pack(pady=10)
        
        tk.Label(
            header,
            text=f"📁 {self.directory}",
            font=('Consolas', 10),
            fg='#ecf0f1',
            bg='#2c3e50'
        ).pack()
        
        # Progress bar
        self.progress_var = tk.DoubleVar()
        self.progress_bar = ttk.Progressbar(
            self.root,
            variable=self.progress_var,
            maximum=100
        )
        self.progress_bar.pack(fill=tk.X, padx=10, pady=5)
        
        # Search and filters
        control_frame = tk.Frame(self.root)
        control_frame.pack(fill=tk.X, padx=10, pady=5)
        
        tk.Label(control_frame, text="🔍", font=('Arial', 14)).pack(side=tk.LEFT)
        self.search_var = tk.StringVar()
        self.search_var.trace('w', self._on_search)
        self.search_entry = ttk.Entry(
            control_frame, 
            textvariable=self.search_var,
            font=('Consolas', 10)
        )
        self.search_entry.pack(side=tk.LEFT, fill=tk.X, expand=True, padx=5)
        
        # Clear search button
        tk.Button(
            control_frame, text="Clear",
            command=lambda: self.search_var.set('')
        ).pack(side=tk.LEFT, padx=2)
        
        # Quick selection
        tk.Button(
            control_frame, text="✅ Check All",
            command=self._select_all,
            bg='#3498db', fg='white'
        ).pack(side=tk.RIGHT, padx=2)
        
        tk.Button(
            control_frame, text="❌ Uncheck All",
            command=self._select_none,
            bg='#95a5a6', fg='white'
        ).pack(side=tk.RIGHT, padx=2)
        
        tk.Button(
            control_frame, text="🔄 Invert",
            command=self._invert_selection,
            bg='#9b59b6', fg='white'
        ).pack(side=tk.RIGHT, padx=2)
        
        # Filter by extension
        tk.Label(control_frame, text="Filter:").pack(side=tk.RIGHT, padx=(10, 2))
        self.filter_var = tk.StringVar(value="All")
        self.filter_combo = ttk.Combobox(
            control_frame,
            textvariable=self.filter_var,
            values=["All"],
            width=12,
            state='readonly'
        )
        self.filter_combo.pack(side=tk.RIGHT, padx=2)
        self.filter_combo.bind('<<ComboboxSelected>>', self._on_filter)
        
        # File list with checkboxes - using regular Listbox with custom rendering
        list_frame = tk.Frame(self.root)
        list_frame.pack(fill=tk.BOTH, expand=True, padx=10, pady=5)
        
        # Create frame for listbox and scrollbars
        self.listbox_frame = tk.Frame(list_frame)
        self.listbox_frame.pack(fill=tk.BOTH, expand=True)
        
        # Create listbox
        self.file_listbox = tk.Listbox(
            self.listbox_frame,
            selectmode=tk.MULTIPLE,
            font=('Consolas', 10),
            activestyle='none'
        )
        
        # Scrollbars
        vsb = ttk.Scrollbar(self.listbox_frame, orient="vertical", command=self.file_listbox.yview)
        hsb = ttk.Scrollbar(self.listbox_frame, orient="horizontal", command=self.file_listbox.xview)
        self.file_listbox.configure(yscrollcommand=vsb.set, xscrollcommand=hsb.set)
        
        # Grid layout
        self.file_listbox.grid(row=0, column=0, sticky='nsew')
        vsb.grid(row=0, column=1, sticky='ns')
        hsb.grid(row=1, column=0, sticky='ew')
        
        self.listbox_frame.grid_rowconfigure(0, weight=1)
        self.listbox_frame.grid_columnconfigure(0, weight=1)
        
        # Bind click event for checkbox toggle
        self.file_listbox.bind("<Button-1>", self._on_listbox_click)
        
        # Summary options
        options_frame = tk.LabelFrame(self.root, text="📊 Summary Options")
        options_frame.pack(fill=tk.X, padx=10, pady=5)
        
        # Summary checkbox
        self.generate_summary_var = tk.BooleanVar(value=False)
        tk.Checkbutton(
            options_frame,
            text="Generate summary file",
            variable=self.generate_summary_var,
            command=self._toggle_summary_options
        ).grid(row=0, column=0, sticky='w', padx=5, pady=2)
        
        # Auto-open checkbox
        self.auto_open_var = tk.BooleanVar(value=True)
        self.auto_open_check = tk.Checkbutton(
            options_frame,
            text="Auto-open summary",
            variable=self.auto_open_var,
            state=tk.DISABLED
        )
        self.auto_open_check.grid(row=0, column=1, sticky='w', padx=5, pady=2)
        
        # Summary mode
        tk.Label(options_frame, text="Mode:").grid(row=1, column=0, sticky='w', padx=5)
        self.mode_var = tk.StringVar(value="detailed")
        self.mode_combo = ttk.Combobox(
            options_frame,
            textvariable=self.mode_var,
            values=["basic", "detailed", "config_query"],
            width=15,
            state='disabled'
        )
        self.mode_combo.grid(row=1, column=1, sticky='w', padx=5, pady=2)
        
        # Truncate size
        tk.Label(options_frame, text="Truncate (KB):").grid(row=1, column=2, sticky='w', padx=5)
        self.truncate_var = tk.IntVar(value=5)
        self.truncate_spin = tk.Spinbox(
            options_frame,
            from_=0, to=100,
            textvariable=self.truncate_var,
            width=10,
            state='disabled'
        )
        self.truncate_spin.grid(row=1, column=3, sticky='w', padx=5, pady=2)
        
        # Status bar
        self.status_frame = tk.Frame(self.root, bg='#34495e', height=30)
        self.status_frame.pack(fill=tk.X)
        
        self.status_label = tk.Label(
            self.status_frame,
            text="Ready",
            fg='white',
            bg='#34495e',
            font=('Consolas', 10)
        )
        self.status_label.pack(side=tk.LEFT, padx=10)
        
        self.selection_label = tk.Label(
            self.status_frame,
            text="0 files selected",
            fg='#3498db',
            bg='#34495e',
            font=('Consolas', 10, 'bold')
        )
        self.selection_label.pack(side=tk.RIGHT, padx=10)
        
        # Action buttons
        button_frame = tk.Frame(self.root)
        button_frame.pack(fill=tk.X, padx=10, pady=10)
        
        self.process_btn = tk.Button(
            button_frame,
            text="⚡ Process Selected",
            command=self._process_files,
            bg='#27ae60',
            fg='white',
            font=('Arial', 12, 'bold'),
            height=2,
            width=20
        )
        self.process_btn.pack(side=tk.LEFT, padx=5)
        
        tk.Button(
            button_frame,
            text="❌ Cancel",
            command=self.root.quit,
            bg='#e74c3c',
            fg='white',
            font=('Arial', 12, 'bold'),
            height=2,
            width=20
        ).pack(side=tk.RIGHT, padx=5)
        
        # Keyboard shortcuts
        self.root.bind('<Control-a>', lambda e: self._select_all())
        self.root.bind('<Control-d>', lambda e: self._select_none())
        self.root.bind('<Control-i>', lambda e: self._invert_selection())
        self.root.bind('<Return>', lambda e: self._process_files())
        self.root.bind('<Escape>', lambda e: self.root.quit())
        
        # Update selection count periodically
        self.root.after(100, self._update_selection_count)
    
    def _toggle_summary_options(self):
        """Enable/disable summary options"""
        state = tk.NORMAL if self.generate_summary_var.get() else tk.DISABLED
        self.auto_open_check.config(state=state)
        self.mode_combo.config(state='readonly' if self.generate_summary_var.get() else 'disabled')
        self.truncate_spin.config(state=state)
    
    def _load_files(self):
        """Load files from directory"""
        self.status_label.config(text="Loading files...")
        self.progress_var.set(0)
        
        try:
            files = sorted([
                f for f in self.directory.iterdir()
                if f.is_file() and not f.name.startswith('.')
            ])
            
            total = len(files)
            extensions = set()
            
            self.file_listbox.delete(0, tk.END)
            
            for i, f in enumerate(files):
                try:
                    info = FileInfo.from_path(f)
                    self.file_infos[f] = info
                    
                    # Add to listbox with formatting
                    display_text = self._format_file_display(f, info, False)
                    self.file_listbox.insert(tk.END, display_text)
                    
                    # Collect extensions
                    extensions.add(f.suffix or 'no ext')
                    
                except Exception as e:
                    logger.error(f"Error loading {f}: {e}")
                
                self.progress_var.set((i + 1) / total * 100)
                self.root.update_idletasks()
            
            # Update filter combo with extensions
            filter_values = ["All"] + sorted(list(extensions))
            self.filter_combo['values'] = filter_values
            
            self.status_label.config(text=f"Loaded {total} files")
            
        except Exception as e:
            messagebox.showerror("Error", f"Failed to load files: {e}")
            self.status_label.config(text="Error loading files")
    
    def _format_file_display(self, file_path: Path, info: FileInfo, checked: bool) -> str:
        """Format file display text"""
        checkbox = self.CHECK_ON if checked else self.CHECK_OFF
        size_str = self._format_size(info.size)
        date_str = info.modified.strftime('%Y-%m-%d %H:%M')
        
        # Fixed width formatting for alignment
        name_part = f"{checkbox} {file_path.name[:40]:<40}"
        size_part = f"{size_str:>10}"
        date_part = f"{date_str:>16}"
        ext_part = f"{(file_path.suffix or 'none'):>8}"
        
        return f"{name_part} | {size_part} | {date_part} | {ext_part}"
    
    def _format_size(self, size: int) -> str:
        """Format file size"""
        for unit in ['B', 'KB', 'MB', 'GB']:
            if size < 1024.0:
                return f"{size:.1f} {unit}"
            size /= 1024.0
        return f"{size:.1f} TB"
    
    def _on_listbox_click(self, event):
        """Handle click on listbox item for checkbox toggle"""
        index = self.file_listbox.nearest(event.y)
        if index >= 0:
            # Get the file at this index
            files = [f for f in self.file_infos.keys()]
            if index < len(files):
                file_path = files[index]
                
                # Toggle checked state
                if file_path in self.checked_files:
                    self.checked_files.discard(file_path)
                else:
                    self.checked_files.add(file_path)
                
                # Update display
                self._refresh_display()
    
    def _refresh_display(self):
        """Refresh the listbox display"""
        # Save current view position
        first = self.file_listbox.yview()[0]
        
        # Clear and repopulate
        self.file_listbox.delete(0, tk.END)
        
        search_term = self.search_var.get().lower()
        filter_ext = self.filter_var.get()
        
        for file_path, info in self.file_infos.items():
            file_name = file_path.name.lower()
            file_ext = file_path.suffix or 'no ext'
            
            # Apply filters
            if search_term and search_term not in file_name:
                continue
            if filter_ext != "All" and file_ext != filter_ext:
                continue
            
            # Add to listbox
            checked = file_path in self.checked_files
            display_text = self._format_file_display(file_path, info, checked)
            self.file_listbox.insert(tk.END, display_text)
        
        # Restore view position
        self.file_listbox.yview_moveto(first)
    
    def _on_search(self, *args):
        """Handle search"""
        self._refresh_display()
    
    def _on_filter(self, event):
        """Handle filter change"""
        self._refresh_display()
    
    def _select_all(self):
        """Select all visible items"""
        search_term = self.search_var.get().lower()
        filter_ext = self.filter_var.get()
        
        for file_path in self.file_infos.keys():
            file_name = file_path.name.lower()
            file_ext = file_path.suffix or 'no ext'
            
            # Only select visible items
            if search_term and search_term not in file_name:
                continue
            if filter_ext != "All" and file_ext != filter_ext:
                continue
            
            self.checked_files.add(file_path)
        
        self._refresh_display()
        self._update_selection_count()
    
    def _select_none(self):
        """Deselect all items"""
        self.checked_files.clear()
        self._refresh_display()
        self._update_selection_count()
    
    def _invert_selection(self):
        """Invert selection"""
        search_term = self.search_var.get().lower()
        filter_ext = self.filter_var.get()
        
        for file_path in self.file_infos.keys():
            file_name = file_path.name.lower()
            file_ext = file_path.suffix or 'no ext'
            
            # Only invert visible items
            if search_term and search_term not in file_name:
                continue
            if filter_ext != "All" and file_ext != filter_ext:
                continue
            
            if file_path in self.checked_files:
                self.checked_files.discard(file_path)
            else:
                self.checked_files.add(file_path)
        
        self._refresh_display()
        self._update_selection_count()
    
    def _update_selection_count(self):
        """Update selection count in status bar"""
        count = len(self.checked_files)
        self.selection_label.config(text=f"{count} files selected")
        
        # Schedule next update
        self.root.after(100, self._update_selection_count)
    
    def _process_files(self):
        """Process selected files"""
        if not self.checked_files:
            messagebox.showwarning("No Selection", "Please select files to process")
            return
        
        # Get selected file paths
        selected_files = list(self.checked_files)
        
        # Disable button during processing
        self.process_btn.config(state=tk.DISABLED)
        self.status_label.config(text=f"Processing {len(selected_files)} files...")
        
        # Run async processing
        self.root.after(100, lambda: self._run_async_processing(selected_files))
    
    def _run_async_processing(self, files: List[Path]):
        """Run async file processing"""
        async def process():
            try:
                # Open files in notepad
                processor = FileProcessor()
                results = await processor.open_files_batch(files)
                
                # Generate summary if requested
                summary_file = None
                if self.generate_summary_var.get():
                    self.status_label.config(text="Generating summary...")
                    
                    # Update config
                    self.summary_config.mode = self.mode_var.get()
                    self.summary_config.truncate_kb = self.truncate_var.get()
                    self.summary_config.auto_open = self.auto_open_var.get()
                    
                    # Generate summary
                    generator = SummaryGenerator(self.summary_config)
                    summary_content = await generator.generate(files, self.directory)
                    
                    # Save to file
                    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
                    summary_file = self.directory / f"file_summary_{timestamp}.md"
                    summary_file.write_text(summary_content, encoding='utf-8')
                    
                    # Open if requested
                    if self.summary_config.auto_open:
                        await processor.open_in_notepad(summary_file)
                
                # Show results
                msg = f"✅ Success: {results['success']} | ❌ Failed: {results['failed']}"
                if summary_file:
                    msg += f"\n\n📊 Summary saved to:\n{summary_file.name}"
                
                self.status_label.config(text=msg)
                
                if results['failed'] == 0:
                    messagebox.showinfo("Success", msg)
                else:
                    messagebox.showwarning("Partial Success", msg)
                
            except Exception as e:
                logger.error(f"Processing error: {e}")
                messagebox.showerror("Error", f"Processing failed: {e}")
                self.status_label.config(text="Error processing files")
            
            finally:
                self.process_btn.config(state=tk.NORMAL)
        
        # Run in asyncio
        try:
            asyncio.run(process())
        except Exception as e:
            logger.error(f"Async processing error: {e}")
            messagebox.showerror("Error", f"Failed to process: {e}")
            self.process_btn.config(state=tk.NORMAL)
    
    def run(self):
        """Run the GUI"""
        self.root.mainloop()

# ==============================================================================
# MAIN ENTRY POINT
# ==============================================================================
def main():
    """Main entry point"""
    import argparse
    
    parser = argparse.ArgumentParser(
        description="Ultimate file selector with checkboxes and filtering"
    )
    parser.add_argument(
        'path',
        nargs='?',
        default='.',
        help='Directory path (default: current)'
    )
    
    args = parser.parse_args()
    
    # Resolve directory
    directory = Path(args.path).resolve()
    
    if not directory.exists():
        print(f"❌ Directory '{directory}' does not exist")
        sys.exit(1)
    
    if not directory.is_dir():
        print(f"❌ '{directory}' is not a directory")
        sys.exit(1)
    
    # Launch GUI
    try:
        app = FileSelectorGUI(directory)
        app.run()
    except Exception as e:
        print(f"❌ Fatal error: {e}")
        logger.exception("Fatal error")
        sys.exit(1)

if __name__ == "__main__":
    main()