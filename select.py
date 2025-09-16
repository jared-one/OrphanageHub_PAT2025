#!/usr/bin/env python3
"""
ultimate_file_selector.py - Enterprise-grade file selector with async operations
Implements Rust-like safety, Go-style concurrency, and functional patterns
"""

import os
import sys
import subprocess
import asyncio
import threading
from concurrent.futures import ThreadPoolExecutor, ProcessPoolExecutor, as_completed
from dataclasses import dataclass, field
from typing import (
    Optional, Union, List, Dict, Tuple, Callable, 
    TypeVar, Generic, Protocol, Final, Literal, 
    AsyncIterator, Awaitable, Any, Set, FrozenSet
)
from pathlib import Path
from datetime import datetime
from enum import Enum, auto
from collections import deque, defaultdict
from functools import wraps, lru_cache, partial, reduce
from itertools import chain, islice, batched
import hashlib
import mmap
import signal
import traceback
import logging
from contextlib import asynccontextmanager, contextmanager
import queue
import multiprocessing as mp

# GUI imports
import tkinter as tk
from tkinter import ttk, messagebox
import tkinter.font as tkfont

# Advanced imports for performance
try:
    import uvloop  # Faster event loop
    asyncio.set_event_loop_policy(uvloop.EventLoopPolicy())
except ImportError:
    pass

# ==============================================================================
# RUST-INSPIRED TYPE SYSTEM
# ==============================================================================

T = TypeVar('T')
E = TypeVar('E')
K = TypeVar('K')
V = TypeVar('V')

class Result(Generic[T, E]):
    """Rust-style Result type for error handling"""
    
    @dataclass(frozen=True)
    class Ok(Generic[T]):
        value: T
        
    @dataclass(frozen=True)
    class Err(Generic[E]):
        error: E
    
    def __init__(self, value: Union[Ok[T], Err[E]]):
        self._value = value
    
    @classmethod
    def ok(cls, value: T) -> 'Result[T, E]':
        return cls(cls.Ok(value))
    
    @classmethod
    def err(cls, error: E) -> 'Result[T, E]':
        return cls(cls.Err(error))
    
    def is_ok(self) -> bool:
        return isinstance(self._value, Result.Ok)
    
    def is_err(self) -> bool:
        return isinstance(self._value, Result.Err)
    
    def unwrap(self) -> T:
        if isinstance(self._value, Result.Ok):
            return self._value.value
        raise ValueError(f"Called unwrap on Err: {self._value.error}")
    
    def unwrap_or(self, default: T) -> T:
        return self._value.value if self.is_ok() else default
    
    def map(self, fn: Callable[[T], 'Result']) -> 'Result':
        return fn(self._value.value) if self.is_ok() else self
    
    def and_then(self, fn: Callable[[T], 'Result']) -> 'Result':
        return fn(self._value.value) if self.is_ok() else self

class Option(Generic[T]):
    """Rust-style Option type for nullable values"""
    
    @dataclass(frozen=True)
    class Some(Generic[T]):
        value: T
    
    class NoneType:
        pass
    
    def __init__(self, value: Optional[T] = None):
        self._value = self.Some(value) if value is not None else self.NoneType()
    
    @classmethod
    def some(cls, value: T) -> 'Option[T]':
        return cls(value)
    
    @classmethod
    def none(cls) -> 'Option[T]':
        return cls(None)
    
    def is_some(self) -> bool:
        return isinstance(self._value, Option.Some)
    
    def is_none(self) -> bool:
        return isinstance(self._value, Option.NoneType)
    
    def unwrap(self) -> T:
        if isinstance(self._value, Option.Some):
            return self._value.value
        raise ValueError("Called unwrap on None")
    
    def unwrap_or(self, default: T) -> T:
        return self._value.value if self.is_some() else default

# ==============================================================================
# ERROR TYPES AND HANDLING
# ==============================================================================

@dataclass(frozen=True)
class FileError:
    """Immutable error type for file operations"""
    path: Path
    operation: str
    message: str
    cause: Optional[Exception] = None
    timestamp: datetime = field(default_factory=datetime.now)
    
    def __str__(self) -> str:
        return f"[{self.timestamp}] {self.operation} failed for {self.path}: {self.message}"

class ErrorHandler:
    """Centralized error handling with recovery strategies"""
    
    def __init__(self):
        self._handlers: Dict[type, List[Callable]] = defaultdict(list)
        self._error_log: deque = deque(maxlen=1000)  # Circular buffer
        self._recovery_strategies: Dict[str, Callable] = {}
        
        # Setup logging
        self.logger = logging.getLogger(__name__)
        handler = logging.StreamHandler()
        handler.setFormatter(logging.Formatter(
            '%(asctime)s - %(name)s - %(levelname)s - %(message)s'
        ))
        self.logger.addHandler(handler)
        self.logger.setLevel(logging.DEBUG)
    
    def register_handler(self, error_type: type, handler: Callable) -> None:
        """Register error handler for specific exception type"""
        self._handlers[error_type].append(handler)
    
    def register_recovery(self, operation: str, strategy: Callable) -> None:
        """Register recovery strategy for operation"""
        self._recovery_strategies[operation] = strategy
    
    async def handle_async(self, error: Exception, context: Dict[str, Any]) -> Result:
        """Async error handling with recovery attempts"""
        self._error_log.append((datetime.now(), error, context))
        self.logger.error(f"Error: {error}", exc_info=True)
        
        # Try specific handlers
        for handler in self._handlers.get(type(error), []):
            try:
                result = await handler(error, context) if asyncio.iscoroutinefunction(handler) else handler(error, context)
                if result:
                    return Result.ok(result)
            except Exception as e:
                self.logger.error(f"Handler failed: {e}")
        
        # Try recovery strategy
        if operation := context.get('operation'):
            if strategy := self._recovery_strategies.get(operation):
                try:
                    result = await strategy(error, context) if asyncio.iscoroutinefunction(strategy) else strategy(error, context)
                    return Result.ok(result)
                except Exception as e:
                    self.logger.error(f"Recovery failed: {e}")
        
        return Result.err(FileError(
            path=context.get('path', Path('.')),
            operation=context.get('operation', 'unknown'),
            message=str(error),
            cause=error
        ))

# ==============================================================================
# GO-STYLE CONCURRENCY WITH CHANNELS
# ==============================================================================

class Channel(Generic[T]):
    """Go-style channel for thread-safe communication"""
    
    def __init__(self, capacity: int = 0):
        self._queue: queue.Queue = queue.Queue(maxsize=capacity)
        self._closed = threading.Event()
        self._lock = threading.RLock()
    
    def send(self, value: T) -> bool:
        """Send value to channel (blocks if full)"""
        if self._closed.is_set():
            return False
        try:
            self._queue.put(value, timeout=0.1)
            return True
        except queue.Full:
            return False
    
    def receive(self) -> Option[T]:
        """Receive from channel (blocks if empty)"""
        if self._closed.is_set() and self._queue.empty():
            return Option.none()
        try:
            value = self._queue.get(timeout=0.1)
            return Option.some(value)
        except queue.Empty:
            return Option.none()
    
    def close(self) -> None:
        """Close the channel"""
        self._closed.set()
    
    def __iter__(self):
        """Iterate over channel values until closed"""
        while not self._closed.is_set() or not self._queue.empty():
            if value := self.receive():
                if value.is_some():
                    yield value.unwrap()

class WorkerPool:
    """Go-style worker pool with channels"""
    
    def __init__(self, num_workers: int = None):
        self.num_workers = num_workers or mp.cpu_count()
        self.tasks: Channel[Callable] = Channel(capacity=1000)
        self.results: Channel[Result] = Channel(capacity=1000)
        self.workers: List[threading.Thread] = []
        self._stop = threading.Event()
        self._start_workers()
    
    def _start_workers(self):
        """Start worker threads"""
        for i in range(self.num_workers):
            worker = threading.Thread(target=self._worker, args=(i,), daemon=True)
            worker.start()
            self.workers.append(worker)
    
    def _worker(self, worker_id: int):
        """Worker thread main loop"""
        while not self._stop.is_set():
            task_opt = self.tasks.receive()
            if task_opt.is_some():
                task = task_opt.unwrap()
                try:
                    result = task()
                    self.results.send(Result.ok(result))
                except Exception as e:
                    self.results.send(Result.err(e))
    
    async def submit_async(self, task: Callable) -> Result:
        """Submit async task to pool"""
        self.tasks.send(task)
        # Wait for result async
        await asyncio.sleep(0)  # Yield control
        result_opt = self.results.receive()
        return result_opt.unwrap() if result_opt.is_some() else Result.err("No result")
    
    def shutdown(self):
        """Graceful shutdown"""
        self._stop.set()
        self.tasks.close()
        for worker in self.workers:
            worker.join(timeout=1)

# ==============================================================================
# IMMUTABLE FILE OPERATIONS
# ==============================================================================

@dataclass(frozen=True)
class FileInfo:
    """Immutable file information"""
    path: Path
    size: int
    modified: datetime
    checksum: Optional[str] = None
    
    @classmethod
    async def from_path(cls, path: Path) -> Result['FileInfo', FileError]:
        """Create FileInfo from path with async I/O"""
        try:
            stat = await asyncio.to_thread(path.stat)
            return Result.ok(cls(
                path=path,
                size=stat.st_size,
                modified=datetime.fromtimestamp(stat.st_mtime),
                checksum=None
            ))
        except Exception as e:
            return Result.err(FileError(path, "stat", str(e), e))
    
    async def compute_checksum(self) -> Result[str, FileError]:
        """Compute file checksum using memory mapping"""
        try:
            def _compute():
                hasher = hashlib.blake2b()  # Faster than SHA
                with open(self.path, 'rb') as f:
                    # Memory map for efficiency
                    with mmap.mmap(f.fileno(), 0, access=mmap.ACCESS_READ) as mm:
                        # Process in chunks
                        chunk_size = 1024 * 1024  # 1MB chunks
                        for i in range(0, len(mm), chunk_size):
                            hasher.update(mm[i:i+chunk_size])
                return hasher.hexdigest()
            
            checksum = await asyncio.to_thread(_compute)
            return Result.ok(checksum)
        except Exception as e:
            return Result.err(FileError(self.path, "checksum", str(e), e))

class AsyncFileProcessor:
    """High-performance async file processor"""
    
    def __init__(self, max_concurrent: int = 10):
        self.semaphore = asyncio.Semaphore(max_concurrent)
        self.error_handler = ErrorHandler()
        self.stats = {
            'processed': 0,
            'failed': 0,
            'bytes_processed': 0
        }
    
    async def process_files(
        self, 
        files: List[Path], 
        operation: Callable[[Path], Awaitable[Result]]
    ) -> List[Result]:
        """Process files concurrently with backpressure control"""
        
        async def _process_with_semaphore(file: Path) -> Result:
            async with self.semaphore:
                try:
                    result = await operation(file)
                    if result.is_ok():
                        self.stats['processed'] += 1
                    else:
                        self.stats['failed'] += 1
                    return result
                except Exception as e:
                    self.stats['failed'] += 1
                    return await self.error_handler.handle_async(e, {
                        'path': file,
                        'operation': 'process_file'
                    })
        
        # Create tasks
        tasks = [_process_with_semaphore(f) for f in files]
        
        # Process with progress tracking
        results = []
        for coro in asyncio.as_completed(tasks):
            result = await coro
            results.append(result)
            yield result  # Stream results
    
    async def open_in_notepad_batch(self, files: List[Path]) -> List[Result]:
        """Open files in notepad with concurrent subprocess handling"""
        
        async def _open_single(file: Path) -> Result:
            try:
                # Convert to Windows path
                proc = await asyncio.create_subprocess_exec(
                    'wslpath', '-w', str(file),
                    stdout=asyncio.subprocess.PIPE,
                    stderr=asyncio.subprocess.PIPE
                )
                stdout, stderr = await proc.communicate()
                
                if proc.returncode != 0:
                    return Result.err(FileError(file, "wslpath", stderr.decode()))
                
                win_path = stdout.decode().strip()
                
                # Launch notepad (fire and forget)
                await asyncio.create_subprocess_exec(
                    'notepad.exe', win_path,
                    stdout=asyncio.subprocess.DEVNULL,
                    stderr=asyncio.subprocess.DEVNULL
                )
                
                return Result.ok(file)
                
            except Exception as e:
                return Result.err(FileError(file, "open_notepad", str(e), e))
        
        results = []
        async for result in self.process_files(files, _open_single):
            results.append(result)
        
        return results

# ==============================================================================
# ADVANCED GUI WITH ASYNC OPERATIONS
# ==============================================================================

class AsyncGUI:
    """Thread-safe GUI with async operations"""
    
    def __init__(self, directory: Path):
        self.directory = directory
        self.selected_files: Set[Path] = set()
        self.file_infos: Dict[Path, FileInfo] = {}
        
        # Thread synchronization
        self.gui_queue: Channel[Callable] = Channel(100)
        self.cancel_event = asyncio.Event()
        
        # Async components
        self.file_processor = AsyncFileProcessor()
        self.worker_pool = WorkerPool()
        
        # GUI setup
        self.root = tk.Tk()
        self.root.title(f"🚀 Ultimate File Selector - {directory}")
        self.root.geometry("1000x700")
        
        # Style configuration
        self.style = ttk.Style()
        self.style.theme_use('clam')
        
        self._setup_gui()
        self._start_async_loop()
    
    def _setup_gui(self):
        """Setup advanced GUI components"""
        
        # Custom fonts
        title_font = tkfont.Font(family="Helvetica", size=14, weight="bold")
        mono_font = tkfont.Font(family="Consolas", size=10)
        
        # Header with gradient effect
        header = tk.Frame(self.root, bg='#1e3d59', height=80)
        header.pack(fill=tk.X)
        
        tk.Label(
            header,
            text="⚡ Ultimate File Selector",
            font=title_font,
            fg='#f5f0e1',
            bg='#1e3d59'
        ).pack(pady=10)
        
        tk.Label(
            header,
            text=f"📁 {self.directory}",
            font=mono_font,
            fg='#ffc13b',
            bg='#1e3d59'
        ).pack()
        
        # Progress bar
        self.progress_var = tk.DoubleVar()
        self.progress_bar = ttk.Progressbar(
            self.root,
            variable=self.progress_var,
            maximum=100,
            mode='determinate',
            style='TProgressbar'
        )
        self.progress_bar.pack(fill=tk.X, padx=10, pady=5)
        
        # Control panel
        control_frame = tk.Frame(self.root, bg='#f5f0e1')
        control_frame.pack(fill=tk.X, padx=10, pady=5)
        
        # Search box
        tk.Label(control_frame, text="🔍", bg='#f5f0e1').pack(side=tk.LEFT)
        self.search_var = tk.StringVar()
        self.search_var.trace('w', self._on_search_changed)
        self.search_entry = ttk.Entry(
            control_frame,
            textvariable=self.search_var,
            font=mono_font
        )
        self.search_entry.pack(side=tk.LEFT, fill=tk.X, expand=True, padx=5)
        
        # Filter buttons
        self.filter_buttons = {}
        for ext, color in [('.py', '#3776ab'), ('.java', '#f89820'), 
                           ('.xml', '#e34c26'), ('.txt', '#555555')]:
            btn = tk.Button(
                control_frame,
                text=ext,
                bg=color,
                fg='white',
                font=('Arial', 9),
                command=lambda e=ext: self._toggle_filter(e)
            )
            btn.pack(side=tk.LEFT, padx=2)
            self.filter_buttons[ext] = btn
        
        # File list with virtual scrolling
        list_frame = tk.Frame(self.root)
        list_frame.pack(fill=tk.BOTH, expand=True, padx=10, pady=5)
        
        # Treeview for better performance
        self.tree = ttk.Treeview(
            list_frame,
            columns=('size', 'modified', 'status'),
            show='tree headings',
            selectmode='extended'
        )
        
        self.tree.heading('#0', text='File')
        self.tree.heading('size', text='Size')
        self.tree.heading('modified', text='Modified')
        self.tree.heading('status', text='Status')
        
        self.tree.column('#0', width=400)
        self.tree.column('size', width=100)
        self.tree.column('modified', width=150)
        self.tree.column('status', width=100)
        
        # Scrollbars
        vsb = ttk.Scrollbar(list_frame, orient="vertical", command=self.tree.yview)
        hsb = ttk.Scrollbar(list_frame, orient="horizontal", command=self.tree.xview)
        self.tree.configure(yscrollcommand=vsb.set, xscrollcommand=hsb.set)
        
        self.tree.grid(row=0, column=0, sticky='nsew')
        vsb.grid(row=0, column=1, sticky='ns')
        hsb.grid(row=1, column=0, sticky='ew')
        
        list_frame.grid_rowconfigure(0, weight=1)
        list_frame.grid_columnconfigure(0, weight=1)
        
        # Status bar
        self.status_frame = tk.Frame(self.root, bg='#1e3d59', height=30)
        self.status_frame.pack(fill=tk.X)
        
        self.status_label = tk.Label(
            self.status_frame,
            text="Ready",
            fg='#f5f0e1',
            bg='#1e3d59',
            font=mono_font
        )
        self.status_label.pack(side=tk.LEFT, padx=10)
        
        # Action buttons
        button_frame = tk.Frame(self.root, bg='#f5f0e1')
        button_frame.pack(fill=tk.X, padx=10, pady=10)
        
        self.submit_btn = tk.Button(
            button_frame,
            text="⚡ Process Selected",
            command=self._on_submit,
            bg='#27ae60',
            fg='white',
            font=title_font,
            height=2,
            width=20,
            cursor='hand2'
        )
        self.submit_btn.pack(side=tk.LEFT, padx=5)
        
        self.cancel_btn = tk.Button(
            button_frame,
            text="⏹ Cancel",
            command=self._on_cancel,
            bg='#e74c3c',
            fg='white',
            font=title_font,
            height=2,
            width=20,
            cursor='hand2',
            state=tk.DISABLED
        )
        self.cancel_btn.pack(side=tk.RIGHT, padx=5)
        
        # Keyboard shortcuts
        self.root.bind('<Control-a>', lambda e: self._select_all())
        self.root.bind('<Control-d>', lambda e: self._select_none())
        self.root.bind('<Control-i>', lambda e: self._invert_selection())
        self.root.bind('<Control-f>', lambda e: self.search_entry.focus())
        self.root.bind('<Escape>', lambda e: self._on_cancel())
    
    def _start_async_loop(self):
        """Start async event loop in background thread"""
        self.loop = asyncio.new_event_loop()
        self.async_thread = threading.Thread(target=self._run_async_loop, daemon=True)
        self.async_thread.start()
        
        # Load files async
        self._schedule_async(self._load_files_async())
    
    def _run_async_loop(self):
        """Run async loop in thread"""
        asyncio.set_event_loop(self.loop)
        self.loop.run_forever()
    
    def _schedule_async(self, coro):
        """Schedule coroutine on async loop"""
        future = asyncio.run_coroutine_threadsafe(coro, self.loop)
        return future
    
    async def _load_files_async(self):
        """Load files asynchronously with progress"""
        try:
            self._update_status("Loading files...")
            self._update_progress(0)
            
            # List files
            files = await asyncio.to_thread(
                lambda: sorted([
                    f for f in self.directory.iterdir()
                    if f.is_file() 
                    and not f.name.startswith('.')
                    and not f.name.endswith('.bak')
                ])
            )
            
            if not files:
                self._update_status("No files found")
                return
            
            # Load file info concurrently
            total = len(files)
            for i, file in enumerate(files):
                result = await FileInfo.from_path(file)
                
                if result.is_ok():
                    info = result.unwrap()
                    self.file_infos[file] = info
                    
                    # Add to tree
                    self._gui_call(lambda: self.tree.insert(
                        '',
                        'end',
                        text=f"  {file.name}",
                        values=(
                            self._format_size(info.size),
                            info.modified.strftime('%Y-%m-%d %H:%M'),
                            '✓'
                        ),
                        tags=(file.suffix,)
                    ))
                
                self._update_progress((i + 1) / total * 100)
            
            self._update_status(f"Loaded {total} files")
            self._update_progress(100)
            
        except Exception as e:
            self._update_status(f"Error: {e}")
    
    def _gui_call(self, func: Callable):
        """Thread-safe GUI update"""
        self.root.after(0, func)
    
    def _update_status(self, text: str):
        """Update status bar"""
        self._gui_call(lambda: self.status_label.config(text=text))
    
    def _update_progress(self, value: float):
        """Update progress bar"""
        self._gui_call(lambda: self.progress_var.set(value))
    
    def _format_size(self, size: int) -> str:
        """Format file size with proper units"""
        for unit in ['B', 'KB', 'MB', 'GB', 'TB']:
            if size < 1024.0:
                return f"{size:.1f} {unit}"
            size /= 1024.0
        return f"{size:.1f} PB"
    
    def _on_search_changed(self, *args):
        """Handle search box changes"""
        query = self.search_var.get().lower()
        
        for item in self.tree.get_children():
            text = self.tree.item(item)['text'].lower()
            if query in text:
                self.tree.item(item, tags=('visible',))
            else:
                self.tree.item(item, tags=('hidden',))
    
    def _toggle_filter(self, ext: str):
        """Toggle file type filter"""
        # Implementation for filtering by extension
        pass
    
    def _select_all(self):
        """Select all visible items"""
        for item in self.tree.get_children():
            self.tree.selection_add(item)
    
    def _select_none(self):
        """Deselect all items"""
        self.tree.selection_remove(self.tree.get_children())
    
    def _invert_selection(self):
        """Invert selection"""
        all_items = set(self.tree.get_children())
        selected = set(self.tree.selection())
        new_selection = all_items - selected
        self.tree.selection_set(list(new_selection))
    
    def _on_submit(self):
        """Handle submit with async processing"""
        selected_items = self.tree.selection()
        if not selected_items:
            messagebox.showwarning("No Selection", "Please select files")
            return
        
        # Get selected file paths
        selected_files = []
        for item in selected_items:
            text = self.tree.item(item)['text'].strip()
            file_path = self.directory / text
            if file_path.exists():
                selected_files.append(file_path)
        
        # Disable buttons
        self.submit_btn.config(state=tk.DISABLED)
        self.cancel_btn.config(state=tk.NORMAL)
        
        # Process async
        self._schedule_async(self._process_files_async(selected_files))
    
    async def _process_files_async(self, files: List[Path]):
        """Process selected files asynchronously"""
        try:
            self._update_status(f"Processing {len(files)} files...")
            self._update_progress(0)
            
            # Open in notepad concurrently
            results = await self.file_processor.open_in_notepad_batch(files)
            
            # Count successes
            successes = sum(1 for r in results if r.is_ok())
            failures = len(results) - successes
            
            # Generate summary
            summary = await self._generate_summary_async(files)
            
            self._update_status(
                f"✅ Processed: {successes} | ❌ Failed: {failures}"
            )
            self._update_progress(100)
            
            # Re-enable buttons
            self._gui_call(lambda: (
                self.submit_btn.config(state=tk.NORMAL),
                self.cancel_btn.config(state=tk.DISABLED)
            ))
            
            # Show results
            if failures > 0:
                errors = [r.unwrap_or("Unknown error") for r in results if r.is_err()]
                messagebox.showwarning(
                    "Some files failed",
                    f"Failed to open {failures} files:\n" + "\n".join(str(e)[:100] for e in errors[:5])
                )
            else:
                messagebox.showinfo("Success", f"Opened {successes} files successfully!")
            
        except Exception as e:
            self._update_status(f"Error: {e}")
            messagebox.showerror("Process Error", str(e))
    
    async def _generate_summary_async(self, files: List[Path]) -> str:
        """Generate summary with async I/O"""
        lines = [
            "# 🚀 ULTIMATE FILE SELECTION SUMMARY",
            f"Generated: {datetime.now().isoformat()}",
            f"Directory: {self.directory}",
            f"Files: {len(files)}",
            "=" * 80,
            ""
        ]
        
        # Process files in parallel
        async def read_file_preview(file: Path) -> str:
            try:
                content = await asyncio.to_thread(
                    lambda: file.read_text(encoding='utf-8', errors='ignore')[:1000]
                )
                return f"### {file.name}\n```\n{content}\n```\n"
            except Exception as e:
                return f"### {file.name}\nError: {e}\n"
        
        # Gather previews concurrently
        previews = await asyncio.gather(*[read_file_preview(f) for f in files])
        lines.extend(previews)
        
        return "\n".join(lines)
    
    def _on_cancel(self):
        """Cancel ongoing operations"""
        self.cancel_event.set()
        self._update_status("Cancelling...")
        self.cancel_btn.config(state=tk.DISABLED)
    
    def run(self):
        """Run the GUI"""
        try:
            self.root.mainloop()
        finally:
            # Cleanup
            self.loop.call_soon_threadsafe(self.loop.stop)
            self.worker_pool.shutdown()
            self.async_thread.join(timeout=1)

# ==============================================================================
# MAIN ENTRY POINT
# ==============================================================================

def main():
    """Main entry point with comprehensive error handling"""
    import argparse
    
    parser = argparse.ArgumentParser(
        description="Ultimate file selector with enterprise-grade concurrency"
    )
    parser.add_argument(
        'path',
        nargs='?',
        default='.',
        help='Directory path (default: current)'
    )
    parser.add_argument(
        '--debug',
        action='store_true',
        help='Enable debug logging'
    )
    
    args = parser.parse_args()
    
    # Setup logging
    if args.debug:
        logging.basicConfig(level=logging.DEBUG)
    
    # Resolve path
    directory = Path(args.path).resolve()
    
    # Validation with Result type
    if not directory.exists():
        print(f"❌ Directory '{directory}' does not exist")
        sys.exit(1)
    
    if not directory.is_dir():
        print(f"❌ '{directory}' is not a directory")
        sys.exit(1)
    
    # Signal handling for graceful shutdown
    def signal_handler(sig, frame):
        print("\n⏹ Shutting down gracefully...")
        sys.exit(0)
    
    signal.signal(signal.SIGINT, signal_handler)
    
    try:
        # Launch application
        app = AsyncGUI(directory)
        app.run()
    except Exception as e:
        print(f"❌ Fatal error: {e}")
        traceback.print_exc()
        sys.exit(1)

if __name__ == "__main__":
    main()
