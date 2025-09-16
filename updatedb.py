#!/usr/bin/env python3
"""
Database Schema Extractor for MS Access Databases (Enhanced Final Version)

Features:
 - Cross-platform tool detection using shutil.which
 - Memory-efficient streaming for large tables
 - Robust parsing for various column name formats
 - HTML-escaped output for security
 - Progress indicators for better UX
 - Comprehensive error handling and logging
 - Multiple output formats (text, json, html, markdown)
 - Database statistics and analysis

Usage: python3 database_schema_extractor.py /path/to/database.mdb -f html -s 10
"""

from __future__ import annotations

import argparse
import csv
import datetime
import html
import json
import logging
import shutil
import subprocess
import sys
import time
from dataclasses import dataclass, asdict
from io import StringIO
from pathlib import Path
from typing import Dict, List, Optional, Set, Tuple
import re

# Configure logging with color support if available
try:
    import colorlog
    handler = colorlog.StreamHandler()
    handler.setFormatter(colorlog.ColoredFormatter(
        '%(log_color)s%(asctime)s - %(levelname)s - %(message)s',
        log_colors={
            'DEBUG': 'cyan',
            'INFO': 'green',
            'WARNING': 'yellow',
            'ERROR': 'red',
            'CRITICAL': 'red,bg_white',
        }
    ))
    logger = colorlog.getLogger(__name__)
    logger.addHandler(handler)
    logger.setLevel(logging.INFO)
except ImportError:
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s - %(levelname)s - %(message)s"
    )
    logger = logging.getLogger(__name__)


@dataclass
class Column:
    """Represents a database column with metadata."""
    name: str
    data_type: str
    size: str
    nullable: bool
    default_value: Optional[str]
    position: int
    is_primary_key: bool = False
    is_foreign_key: bool = False
    referenced_table: Optional[str] = None
    referenced_column: Optional[str] = None


@dataclass
class Index:
    """Represents a database index."""
    name: str
    columns: List[str]
    is_unique: bool
    is_primary: bool


@dataclass
class Relationship:
    """Represents a foreign key relationship."""
    name: str
    from_table: str
    from_column: str
    to_table: str
    to_column: str
    on_delete: str = "NO ACTION"
    on_update: str = "NO ACTION"


@dataclass
class Table:
    """Represents a database table with full metadata."""
    name: str
    columns: List[Column]
    row_count: int
    sample_data: List[Dict]
    indexes: List[Index] = None
    relationships: List[Relationship] = None
    estimated_size_bytes: int = 0
    
    def __post_init__(self):
        if self.indexes is None:
            self.indexes = []
        if self.relationships is None:
            self.relationships = []


@dataclass
class DatabaseStats:
    """Overall database statistics."""
    total_tables: int
    total_columns: int
    total_rows: int
    total_indexes: int
    total_relationships: int
    extraction_time_seconds: float
    database_size_bytes: int = 0


class ProgressIndicator:
    """Simple progress indicator for console output."""
    
    def __init__(self, total: int, description: str = "Processing"):
        self.total = total
        self.current = 0
        self.description = description
        self.start_time = time.time()
    
    def update(self, increment: int = 1):
        """Update progress."""
        self.current += increment
        if self.total > 0:
            percentage = (self.current / self.total) * 100
            elapsed = time.time() - self.start_time
            if self.current > 0:
                eta = (elapsed / self.current) * (self.total - self.current)
                eta_str = f"ETA: {eta:.1f}s"
            else:
                eta_str = ""
            
            # Simple progress bar
            bar_length = 40
            filled = int(bar_length * self.current / self.total)
            bar = '█' * filled + '░' * (bar_length - filled)
            
            print(f"\r{self.description}: [{bar}] {percentage:.1f}% {eta_str}", end="", flush=True)
    
    def finish(self):
        """Mark as complete."""
        print()  # New line after progress bar


class DatabaseSchemaExtractor:
    """Extract comprehensive schema information from MS Access databases."""

    def __init__(self, db_path: str, sample_rows: int = 5, timeout: int = 30):
        """
        Initialize the extractor.
        
        Args:
            db_path: Path to the Access database file
            sample_rows: Number of sample rows to extract per table
            timeout: Command timeout in seconds
        """
        self.db_path = Path(db_path)
        self.sample_rows = max(0, int(sample_rows))
        self.timeout = max(10, int(timeout))
        self.extraction_start_time = None
        self._validate_environment()

    def _validate_environment(self) -> None:
        """Validate inputs and required external tooling."""
        if not self.db_path.exists():
            raise FileNotFoundError(f"Database file not found: {self.db_path}")

        if self.db_path.suffix.lower() not in [".mdb", ".accdb"]:
            raise ValueError(f"Invalid database file extension: {self.db_path.suffix}")

        # Check for required mdbtools binaries
        required_tools = ["mdb-tables", "mdb-schema", "mdb-export"]
        missing_tools = []
        
        for tool in required_tools:
            if shutil.which(tool) is None:
                missing_tools.append(tool)
        
        if missing_tools:
            raise EnvironmentError(
                f"Required tools not found: {', '.join(missing_tools)}\n"
                f"Please install mdbtools:\n"
                f"  Ubuntu/Debian: sudo apt-get install mdbtools\n"
                f"  macOS: brew install mdbtools\n"
                f"  Other: Check your package manager or build from source"
            )

        logger.debug("All required mdbtools detected in PATH")
        
        # Get database file size
        self.db_size = self.db_path.stat().st_size
        logger.info(f"Database size: {self._format_bytes(self.db_size)}")

    def _format_bytes(self, size: int) -> str:
        """Format bytes to human readable string."""
        for unit in ['B', 'KB', 'MB', 'GB']:
            if size < 1024.0:
                return f"{size:.2f} {unit}"
            size /= 1024.0
        return f"{size:.2f} TB"

    def _run_command(self, cmd: List[str], check_only: bool = False) -> str:
        """
        Run a command safely and return stdout.
        
        Args:
            cmd: Command as list of arguments
            check_only: If True, don't raise on failure
            
        Returns:
            Command output as string
        """
        try:
            logger.debug("Running command: %s", " ".join(cmd))
            proc = subprocess.run(
                cmd,
                capture_output=True,
                text=True,
                check=True,
                timeout=self.timeout,
            )
            return proc.stdout.strip()

        except subprocess.CalledProcessError as e:
            if not check_only:
                logger.error("Command failed: %s\nError: %s", " ".join(cmd), e.stderr)
                raise
            return ""
        except subprocess.TimeoutExpired:
            logger.error("Command timed out after %ds: %s", self.timeout, " ".join(cmd))
            if not check_only:
                raise
            return ""

    def _run_command_stream(self, cmd: List[str]):
        """Run a command and yield lines from stdout (memory-efficient streaming)."""
        logger.debug("Streaming command: %s", " ".join(cmd))
        try:
            with subprocess.Popen(
                cmd, 
                stdout=subprocess.PIPE, 
                stderr=subprocess.PIPE, 
                text=True
            ) as process:
                assert process.stdout is not None
                for line in process.stdout:
                    yield line.rstrip("\n")
                
                process.wait(timeout=self.timeout)
                if process.returncode != 0:
                    stderr = process.stderr.read() if process.stderr else ""
                    logger.error("Command failed (stream): %s - %s", " ".join(cmd), stderr)
        except subprocess.TimeoutExpired:
            logger.error("Streaming command timed out: %s", " ".join(cmd))
            if process:
                process.kill()
            return

    def get_tables(self) -> List[str]:
        """Get list of user tables from the database."""
        logger.info("Fetching table list...")

        output = self._run_command(["mdb-tables", "-1", str(self.db_path)])
        if not output:
            return []

        # Filter system tables and clean names
        tables = [
            t.strip() for t in output.splitlines() 
            if t.strip() and not t.strip().startswith("MSys")
        ]
        
        logger.info("Found %d user tables", len(tables))
        return sorted(tables)

    def get_table_schema(self, table_name: str) -> Tuple[List[Column], List[Index]]:
        """Get complete schema details for a specific table."""
        logger.debug("Fetching schema for table: %s", table_name)

        output = self._run_command([
            "mdb-schema",
            str(self.db_path),
            "-T",
            table_name,
        ])

        if not output:
            return [], []

        columns: List[Column] = []
        indexes: List[Index] = []
        primary_key_columns: Set[str] = set()
        
        in_create_table = False
        in_indexes = False
        position = 0

        for raw_line in output.splitlines():
            line = raw_line.strip()

            # Parse CREATE TABLE section
            if line.upper().startswith("CREATE TABLE"):
                in_create_table = True
                in_indexes = False
                continue

            # Parse CREATE INDEX sections
            if "CREATE" in line.upper() and "INDEX" in line.upper():
                in_indexes = True
                in_create_table = False
                index = self._parse_index_definition(line)
                if index:
                    indexes.append(index)
                    if index.is_primary:
                        primary_key_columns.update(index.columns)
                continue

            if in_create_table:
                if line.startswith(");"):
                    in_create_table = False
                    continue

                if not line:
                    continue

                # Check for PRIMARY KEY constraint
                if "PRIMARY KEY" in line.upper():
                    pk_match = re.search(r'PRIMARY KEY\s*KATEX_INLINE_OPEN([^)]+)KATEX_INLINE_CLOSE', line, re.IGNORECASE)
                    if pk_match:
                        pk_cols = [col.strip().strip('[]"') for col in pk_match.group(1).split(',')]
                        primary_key_columns.update(pk_cols)
                    continue

                # Parse column definition
                col = self._parse_column_definition(line, position)
                if col:
                    columns.append(col)
                    position += 1

        # Mark primary key columns
        for col in columns:
            if col.name in primary_key_columns:
                col.is_primary_key = True

        return columns, indexes

    def _parse_column_definition(self, line: str, position: int) -> Optional[Column]:
        """Parse a single column definition from CREATE TABLE statement."""
        try:
            line = line.rstrip(",").strip()
            
            # Skip constraint definitions
            if any(keyword in line.upper() for keyword in ["CONSTRAINT", "PRIMARY KEY", "FOREIGN KEY"]):
                return None

            # Extract column name (handling brackets, quotes, backticks)
            name = None
            rest = ""
            
            # Handle [ColumnName] format
            if line.startswith("["):
                end_idx = line.find("]")
                if end_idx == -1:
                    return None
                name = line[1:end_idx]
                rest = line[end_idx + 1:].strip()
            # Handle "ColumnName" format
            elif line.startswith('"'):
                end_idx = line.find('"', 1)
                if end_idx == -1:
                    return None
                name = line[1:end_idx]
                rest = line[end_idx + 1:].strip()
            # Handle `ColumnName` format
            elif line.startswith('`'):
                end_idx = line.find('`', 1)
                if end_idx == -1:
                    return None
                name = line[1:end_idx]
                rest = line[end_idx + 1:].strip()
            # Handle unquoted names
            else:
                parts = line.split(None, 1)
                if not parts:
                    return None
                name = parts[0]
                rest = parts[1] if len(parts) > 1 else ""

            # Parse data type and size
            type_match = re.match(r'(\w+)(?:KATEX_INLINE_OPEN([^)]+)KATEX_INLINE_CLOSE)?', rest)
            if type_match:
                data_type = type_match.group(1)
                size = type_match.group(2) or ""
            else:
                data_type = "TEXT"
                size = ""

            # Check constraints
            rest_upper = rest.upper()
            nullable = "NOT NULL" not in rest_upper
            
            # Extract default value
            default_value = None
            default_match = re.search(r'DEFAULT\s+([^\s,]+)', rest, re.IGNORECASE)
            if default_match:
                default_value = default_match.group(1).strip("'\"")

            return Column(
                name=name,
                data_type=data_type,
                size=size,
                nullable=nullable,
                default_value=default_value,
                position=position + 1,
            )

        except Exception as e:
            logger.warning("Failed to parse column definition: %s - %s", line, e)
            return None

    def _parse_index_definition(self, line: str) -> Optional[Index]:
        """Parse index definition from CREATE INDEX statement."""
        try:
            # Parse CREATE [UNIQUE] INDEX index_name ON table_name (columns)
            match = re.search(
                r'CREATE\s+(UNIQUE\s+)?INDEX\s+(\w+)\s+ON\s+\w+\s*KATEX_INLINE_OPEN([^)]+)KATEX_INLINE_CLOSE',
                line,
                re.IGNORECASE
            )
            if match:
                is_unique = bool(match.group(1))
                index_name = match.group(2)
                columns = [col.strip().strip('[]"') for col in match.group(3).split(',')]
                
                return Index(
                    name=index_name,
                    columns=columns,
                    is_unique=is_unique,
                    is_primary="PRIMARY" in index_name.upper()
                )
        except Exception as e:
            logger.warning("Failed to parse index definition: %s - %s", line, e)
        return None

    def get_relationships(self) -> List[Relationship]:
        """Extract foreign key relationships from the database."""
        logger.debug("Fetching relationships...")
        relationships = []
        
        try:
            output = self._run_command([
                "mdb-schema",
                str(self.db_path),
                "--no-indexes"
            ])
            
            # Parse REFERENCES clauses
            for line in output.splitlines():
                if "REFERENCES" in line.upper():
                    rel = self._parse_relationship(line)
                    if rel:
                        relationships.append(rel)
        except Exception as e:
            logger.warning("Could not extract relationships: %s", e)
        
        return relationships

    def _parse_relationship(self, line: str) -> Optional[Relationship]:
        """Parse foreign key relationship from schema output."""
        try:
            # Parse FOREIGN KEY (column) REFERENCES table(column)
            match = re.search(
                r'FOREIGN KEY\s*KATEX_INLINE_OPEN([^)]+)KATEX_INLINE_CLOSE\s+REFERENCES\s+(\w+)\s*KATEX_INLINE_OPEN([^)]+)KATEX_INLINE_CLOSE',
                line,
                re.IGNORECASE
            )
            if match:
                from_column = match.group(1).strip().strip('[]"')
                to_table = match.group(2).strip()
                to_column = match.group(3).strip().strip('[]"')
                
                # Try to extract table name from context
                from_table = "Unknown"  # Would need context from CREATE TABLE
                
                return Relationship(
                    name=f"FK_{from_column}_{to_table}",
                    from_table=from_table,
                    from_column=from_column,
                    to_table=to_table,
                    to_column=to_column
                )
        except Exception as e:
            logger.debug("Could not parse relationship: %s - %s", line, e)
        return None

    def get_row_count(self, table_name: str) -> int:
        """Get the total row count for a table efficiently."""
        try:
            cmd = ["mdb-export", "-H", str(self.db_path), table_name]
            line_gen = self._run_command_stream(cmd)
            
            count = -1  # Start at -1 to exclude header
            for _ in line_gen:
                count += 1
            
            return max(0, count)
        except Exception as e:
            logger.warning("Failed to get row count for %s: %s", table_name, e)
            return 0

    def get_sample_data(self, table_name: str) -> List[Dict]:
        """Get sample data from a table."""
        if self.sample_rows <= 0:
            return []
            
        logger.debug("Fetching sample data for table: %s", table_name)
        
        try:
            output = self._run_command([
                "mdb-export",
                "-H",
                "-d", ",",
                "-q", '"',
                str(self.db_path),
                table_name,
            ])

            if not output:
                return []

            reader = csv.DictReader(StringIO(output))
            rows: List[Dict] = []
            
            for i, row in enumerate(reader):
                if i >= self.sample_rows:
                    break
                # Convert empty strings to None
                cleaned_row = {
                    k: (v if v != "" else None) 
                    for k, v in row.items()
                }
                rows.append(cleaned_row)

            return rows
        except Exception as e:
            logger.warning("Failed to get sample data for %s: %s", table_name, e)
            return []

    def extract_all(self) -> Tuple[Dict[str, Table], DatabaseStats]:
        """
        Extract complete schema information for all tables.
        
        Returns:
            Tuple of (tables dictionary, database statistics)
        """
        self.extraction_start_time = time.time()
        tables_dict: Dict[str, Table] = {}
        table_names = self.get_tables()
        
        # Initialize statistics
        total_rows = 0
        total_columns = 0
        total_indexes = 0
        
        # Progress indicator
        progress = ProgressIndicator(len(table_names), "Extracting tables")

        for table_name in table_names:
            logger.info("Processing table: %s", table_name)
            progress.update()
            
            try:
                columns, indexes = self.get_table_schema(table_name)
                row_count = self.get_row_count(table_name)
                sample_data = self.get_sample_data(table_name) if self.sample_rows > 0 else []
                
                # Estimate table size (rough approximation)
                avg_row_size = 100  # bytes, rough estimate
                if sample_data:
                    # Better estimate based on sample
                    sample_size = sum(
                        len(str(v)) if v is not None else 0
                        for row in sample_data
                        for v in row.values()
                    ) / len(sample_data)
                    avg_row_size = int(sample_size * 1.5)  # Add overhead
                
                estimated_size = row_count * avg_row_size

                tables_dict[table_name] = Table(
                    name=table_name,
                    columns=columns,
                    row_count=row_count,
                    sample_data=sample_data,
                    indexes=indexes,
                    estimated_size_bytes=estimated_size
                )
                
                # Update statistics
                total_rows += row_count
                total_columns += len(columns)
                total_indexes += len(indexes)
                
            except Exception as e:
                logger.error("Failed to process table %s: %s", table_name, e)
                continue
        
        progress.finish()
        
        # Get relationships
        relationships = self.get_relationships()
        
        # Assign relationships to tables
        for rel in relationships:
            if rel.from_table in tables_dict:
                tables_dict[rel.from_table].relationships.append(rel)
        
        extraction_time = time.time() - self.extraction_start_time
        
        stats = DatabaseStats(
            total_tables=len(tables_dict),
            total_columns=total_columns,
            total_rows=total_rows,
            total_indexes=total_indexes,
            total_relationships=len(relationships),
            extraction_time_seconds=extraction_time,
            database_size_bytes=self.db_size
        )
        
        logger.info("Extraction completed in %.2f seconds", extraction_time)
        return tables_dict, stats

    def generate_report(self, output_path: str, format: str = "text") -> None:
        """Generate a detailed report of the database schema."""
        logger.info("Generating %s report...", format)
        
        tables, stats = self.extract_all()
        
        output_path = Path(output_path)
        output_path.parent.mkdir(parents=True, exist_ok=True)

        if format == "json":
            self._generate_json_report(tables, stats, output_path)
        elif format == "html":
            self._generate_html_report(tables, stats, output_path)
        elif format == "markdown":
            self._generate_markdown_report(tables, stats, output_path)
        else:
            self._generate_text_report(tables, stats, output_path)

        logger.info("Report saved to: %s", output_path)
        logger.info("Report size: %s", self._format_bytes(output_path.stat().st_size))

    def _generate_text_report(self, tables: Dict[str, Table], stats: DatabaseStats, output_path: Path) -> None:
        """Generate a detailed text format report."""
        with output_path.open("w", encoding="utf-8") as f:
            # Header
            f.write("=" * 80 + "\n")
            f.write("DATABASE SCHEMA DOCUMENTATION\n")
            f.write("=" * 80 + "\n\n")
            
            f.write(f"Database: {self.db_path.name}\n")
            f.write(f"Full Path: {self.db_path.absolute()}\n")
            f.write(f"Database Size: {self._format_bytes(stats.database_size_bytes)}\n")
            f.write(f"Generated: {datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
            f.write(f"Extraction Time: {stats.extraction_time_seconds:.2f} seconds\n\n")
            
            # Statistics
            f.write("-" * 80 + "\n")
            f.write("DATABASE STATISTICS\n")
            f.write("-" * 80 + "\n")
            f.write(f"Total Tables: {stats.total_tables}\n")
            f.write(f"Total Columns: {stats.total_columns}\n")
            f.write(f"Total Rows: {stats.total_rows:,}\n")
            f.write(f"Total Indexes: {stats.total_indexes}\n")
            f.write(f"Total Relationships: {stats.total_relationships}\n\n")
            
            # Table of Contents
            f.write("-" * 80 + "\n")
            f.write("TABLE OF CONTENTS\n")
            f.write("-" * 80 + "\n\n")
            
            for i, table_name in enumerate(sorted(tables.keys()), 1):
                table = tables[table_name]
                size_str = self._format_bytes(table.estimated_size_bytes)
                f.write(f"{i:3}. {table_name:<40} ({table.row_count:,} rows, ~{size_str})\n")
            
            # Detailed Schema
            f.write("\n" + "=" * 80 + "\n")
            f.write("DETAILED SCHEMA\n")
            f.write("=" * 80 + "\n\n")
            
            for table_name in sorted(tables.keys()):
                table = tables[table_name]
                
                f.write("\n" + "-" * 80 + "\n")
                f.write(f"TABLE: {table_name}\n")
                f.write("-" * 80 + "\n\n")
                
                f.write(f"Total Rows: {table.row_count:,}\n")
                f.write(f"Total Columns: {len(table.columns)}\n")
                f.write(f"Estimated Size: {self._format_bytes(table.estimated_size_bytes)}\n\n")
                
                # Columns
                f.write("COLUMNS:\n")
                f.write("-" * 40 + "\n")
                
                for col in table.columns:
                    f.write(f"  [{col.position:2}] {col.name:<30}")
                    if col.is_primary_key:
                        f.write(" [PK]")
                    if col.is_foreign_key:
                        f.write(" [FK]")
                    f.write("\n")
                    
                    f.write(f"      Type: {col.data_type}")
                    if col.size:
                        f.write(f"({col.size})")
                    f.write(f"\n      Nullable: {'Yes' if col.nullable else 'No'}")
                    if col.default_value:
                        f.write(f"\n      Default: {col.default_value}")
                    if col.referenced_table:
                        f.write(f"\n      References: {col.referenced_table}.{col.referenced_column}")
                    f.write("\n\n")
                
                # Indexes
                if table.indexes:
                    f.write("\nINDEXES:\n")
                    f.write("-" * 40 + "\n")
                    for idx in table.indexes:
                        f.write(f"  {idx.name}\n")
                        f.write(f"    Columns: {', '.join(idx.columns)}\n")
                        f.write(f"    Unique: {'Yes' if idx.is_unique else 'No'}\n")
                        f.write(f"    Primary: {'Yes' if idx.is_primary else 'No'}\n\n")
                
                # Relationships
                if table.relationships:
                    f.write("\nRELATIONSHIPS:\n")
                    f.write("-" * 40 + "\n")
                    for rel in table.relationships:
                        f.write(f"  {rel.from_column} -> {rel.to_table}.{rel.to_column}\n")
                
                # Sample data
                if table.sample_data:
                    f.write(f"\nSAMPLE DATA (First {len(table.sample_data)} rows):\n")
                    f.write("-" * 40 + "\n")
                    
                    for i, row in enumerate(table.sample_data, 1):
                        f.write(f"\nRow {i}:\n")
                        for key, value in row.items():
                            val_str = str(value) if value is not None else "NULL"
                            if len(val_str) > 100:
                                val_str = val_str[:97] + "..."
                            f.write(f"  {key}: {val_str}\n")
                else:
                    f.write("\nNo sample data available.\n")
                
                f.write("\n")

    def _generate_json_report(self, tables: Dict[str, Table], stats: DatabaseStats, output_path: Path) -> None:
        """Generate a JSON format report."""
        output = {
            "database": {
                "name": self.db_path.name,
                "path": str(self.db_path.absolute()),
                "size_bytes": stats.database_size_bytes,
                "generated": datetime.datetime.now().isoformat(),
                "extraction_time_seconds": stats.extraction_time_seconds
            },
            "statistics": asdict(stats),
            "tables": {}
        }

        for table_name, table in tables.items():
            output["tables"][table_name] = {
                "row_count": table.row_count,
                "estimated_size_bytes": table.estimated_size_bytes,
                "columns": [
                    {
                        "position": col.position,
                        "name": col.name,
                        "type": col.data_type,
                        "size": col.size,
                        "nullable": col.nullable,
                        "default": col.default_value,
                        "is_primary_key": col.is_primary_key,
                        "is_foreign_key": col.is_foreign_key,
                        "referenced_table": col.referenced_table,
                        "referenced_column": col.referenced_column
                    }
                    for col in table.columns
                ],
                "indexes": [
                    {
                        "name": idx.name,
                        "columns": idx.columns,
                        "is_unique": idx.is_unique,
                        "is_primary": idx.is_primary
                    }
                    for idx in table.indexes
                ],
                "relationships": [
                    asdict(rel) for rel in table.relationships
                ],
                "sample_data": table.sample_data
            }

        with output_path.open("w", encoding="utf-8") as f:
            json.dump(output, f, indent=2, default=str)

    def _generate_html_report(self, tables: Dict[str, Table], stats: DatabaseStats, output_path: Path) -> None:
        """Generate a professional HTML format report with enhanced styling."""
        html_parts = []
        
        # HTML header with enhanced CSS
        html_parts.append("""<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Database Schema Report</title>
    <style>
        :root {
            --primary-color: #2563eb;
            --success-color: #16a34a;
            --warning-color: #ea580c;
            --danger-color: #dc2626;
            --dark: #1f2937;
            --light: #f9fafb;
            --border: #e5e7eb;
        }
        
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
            line-height: 1.6;
            color: var(--dark);
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
        }
        
        .container {
            max-width: 1400px;
            margin: 0 auto;
            padding: 20px;
        }
        
        .header {
            background: white;
            border-radius: 12px;
            padding: 30px;
            margin-bottom: 30px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.1);
        }
        
        .header h1 {
            color: var(--primary-color);
            margin-bottom: 10px;
            font-size: 2.5em;
        }
        
        .header .meta {
            color: #6b7280;
            font-size: 0.95em;
        }
        
        .header .meta-item {
            display: inline-block;
            margin-right: 20px;
            padding: 5px 0;
        }
        
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        
        .stat-card {
            background: white;
            padding: 20px;
            border-radius: 12px;
            text-align: center;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            transition: transform 0.3s, box-shadow 0.3s;
        }
        
        .stat-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 10px 20px rgba(0,0,0,0.15);
        }
        
        .stat-number {
            font-size: 2.5em;
            font-weight: bold;
            color: var(--primary-color);
            margin-bottom: 5px;
        }
        
        .stat-label {
            color: #6b7280;
            font-size: 0.9em;
            text-transform: uppercase;
            letter-spacing: 1px;
        }
        
        .toc {
            background: white;
            padding: 25px;
            border-radius: 12px;
            margin-bottom: 30px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        }
        
        .toc h2 {
            color: var(--dark);
            margin-bottom: 20px;
            padding-bottom: 10px;
            border-bottom: 2px solid var(--border);
        }
        
        .toc-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
            gap: 15px;
        }
        
        .toc-item {
            padding: 12px;
            background: var(--light);
            border-radius: 8px;
            border-left: 4px solid var(--primary-color);
            transition: all 0.3s;
        }
        
        .toc-item:hover {
            background: white;
            border-left-color: var(--success-color);
            transform: translateX(5px);
        }
        
        .toc-item a {
            color: var(--dark);
            text-decoration: none;
            font-weight: 600;
        }
        
        .toc-item .meta {
            color: #6b7280;
            font-size: 0.85em;
            margin-top: 4px;
        }
        
        .table-info {
            background: white;
            margin-bottom: 30px;
            border-radius: 12px;
            overflow: hidden;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        }
        
        .table-header {
            background: linear-gradient(135deg, var(--primary-color), #3b82f6);
            color: white;
            padding: 20px 25px;
        }
        
        .table-header h2 {
            margin-bottom: 10px;
        }
        
        .table-header .meta {
            opacity: 0.9;
            font-size: 0.9em;
        }
        
        .table-body {
            padding: 25px;
        }
        
        .schema-table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 25px;
        }
        
        .schema-table th {
            background: var(--light);
            padding: 12px;
            text-align: left;
            font-weight: 600;
            color: var(--dark);
            border-bottom: 2px solid var(--border);
        }
        
        .schema-table td {
            padding: 12px;
            border-bottom: 1px solid var(--border);
        }
        
        .schema-table tr:hover {
            background: #f3f4f6;
        }
        
        .badge {
            display: inline-block;
            padding: 3px 8px;
            border-radius: 4px;
            font-size: 0.75em;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        
        .badge-pk {
            background: #dbeafe;
            color: var(--primary-color);
        }
        
        .badge-fk {
            background: #fef3c7;
            color: #92400e;
        }
        
        .badge-unique {
            background: #ede9fe;
            color: #7c3aed;
        }
        
        .badge-nullable {
            background: #d1fae5;
            color: #065f46;
        }
        
        .badge-not-null {
            background: #fee2e2;
            color: #991b1b;
        }
        
        .type-badge {
            background: #e0e7ff;
            color: #3730a3;
            font-family: 'Courier New', monospace;
        }
        
        .sample-data {
            background: var(--light);
            border-radius: 8px;
            padding: 15px;
            margin: 10px 0;
            border-left: 4px solid var(--success-color);
        }
        
        .sample-data strong {
            color: var(--primary-color);
        }
        
        .sample-data .row-header {
            background: white;
            padding: 8px;
            margin: -15px -15px 10px -15px;
            border-bottom: 1px solid var(--border);
            font-weight: 600;
        }
        
        .sample-data .field {
            margin: 5px 0;
            word-break: break-word;
        }
        
        .sample-data .field-name {
            font-weight: 600;
            color: #4b5563;
        }
        
        .sample-data .field-value {
            color: #111827;
        }
        
        .sample-data .null-value {
            color: #9ca3af;
            font-style: italic;
        }
        
        .index-section, .relationship-section {
            margin-top: 25px;
            padding-top: 20px;
            border-top: 2px solid var(--border);
        }
        
        .index-item, .relationship-item {
            background: var(--light);
            padding: 12px;
            border-radius: 6px;
            margin: 10px 0;
        }
        
        .search-box {
            padding: 12px;
            width: 100%;
            border: 2px solid var(--border);
            border-radius: 8px;
            font-size: 1em;
            margin-bottom: 20px;
        }
        
        .search-box:focus {
            outline: none;
            border-color: var(--primary-color);
        }
        
        @media (max-width: 768px) {
            .stats-grid {
                grid-template-columns: 1fr;
            }
            
            .toc-grid {
                grid-template-columns: 1fr;
            }
        }
        
        @media print {
            body {
                background: white;
            }
            
            .table-info {
                page-break-inside: avoid;
            }
        }
    </style>
    <script>
        function searchTables() {
            const searchTerm = document.getElementById('tableSearch').value.toLowerCase();
            const tables = document.querySelectorAll('.table-info');
            const tocItems = document.querySelectorAll('.toc-item');
            
            tables.forEach((table, index) => {
                const tableName = table.id.toLowerCase();
                const shouldShow = tableName.includes(searchTerm);
                table.style.display = shouldShow ? 'block' : 'none';
                tocItems[index].style.display = shouldShow ? 'block' : 'none';
            });
        }
    </script>
</head>
<body>
    <div class="container">
""")

        # Header section
        html_parts.append(f"""
        <div class="header">
            <h1>📊 Database Schema Documentation</h1>
            <div class="meta">
                <div class="meta-item">📁 <strong>Database:</strong> {html.escape(self.db_path.name)}</div>
                <div class="meta-item">💾 <strong>Size:</strong> {self._format_bytes(stats.database_size_bytes)}</div>
                <div class="meta-item">📅 <strong>Generated:</strong> {datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')}</div>
                <div class="meta-item">⏱️ <strong>Extraction Time:</strong> {stats.extraction_time_seconds:.2f}s</div>
            </div>
        </div>
""")

        # Statistics cards
        html_parts.append("""
        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-number">{}</div>
                <div class="stat-label">Tables</div>
            </div>
            <div class="stat-card">
                <div class="stat-number">{:,}</div>
                <div class="stat-label">Total Rows</div>
            </div>
            <div class="stat-card">
                <div class="stat-number">{}</div>
                <div class="stat-label">Columns</div>
            </div>
            <div class="stat-card">
                <div class="stat-number">{}</div>
                <div class="stat-label">Indexes</div>
            </div>
            <div class="stat-card">
                <div class="stat-number">{}</div>
                <div class="stat-label">Relationships</div>
            </div>
        </div>
""".format(
            stats.total_tables,
            stats.total_rows,
            stats.total_columns,
            stats.total_indexes,
            stats.total_relationships
        ))

        # Table of Contents with search
        html_parts.append("""
        <div class="toc">
            <h2>📋 Tables Overview</h2>
            <input type="text" id="tableSearch" class="search-box" placeholder="Search tables..." onkeyup="searchTables()">
            <div class="toc-grid">
""")

        for table_name in sorted(tables.keys()):
            table = tables[table_name]
            html_parts.append(f"""
                <div class="toc-item">
                    <a href="#{html.escape(table_name)}">{html.escape(table_name)}</a>
                    <div class="meta">
                        {table.row_count:,} rows • {len(table.columns)} columns • ~{self._format_bytes(table.estimated_size_bytes)}
                    </div>
                </div>
""")

        html_parts.append("""
            </div>
        </div>
""")

        # Table details
        for table_name in sorted(tables.keys()):
            table = tables[table_name]
            
            html_parts.append(f"""
        <div class="table-info" id="{html.escape(table_name)}">
            <div class="table-header">
                <h2>{html.escape(table_name)}</h2>
                <div class="meta">
                    {table.row_count:,} rows • {len(table.columns)} columns • ~{self._format_bytes(table.estimated_size_bytes)}
                </div>
            </div>
            <div class="table-body">
                <h3>Schema</h3>
                <table class="schema-table">
                    <thead>
                        <tr>
                            <th style="width: 50px">#</th>
                            <th>Column Name</th>
                            <th>Data Type</th>
                            <th style="width: 100px">Size</th>
                            <th style="width: 100px">Nullable</th>
                            <th>Default</th>
                            <th style="width: 100px">Keys</th>
                        </tr>
                    </thead>
                    <tbody>
""")

            for col in table.columns:
                nullable_badge = '<span class="badge badge-nullable">NULL</span>' if col.nullable else '<span class="badge badge-not-null">NOT NULL</span>'
                
                keys = []
                if col.is_primary_key:
                    keys.append('<span class="badge badge-pk">PK</span>')
                if col.is_foreign_key:
                    keys.append('<span class="badge badge-fk">FK</span>')
                keys_html = ' '.join(keys) if keys else '-'
                
                html_parts.append(f"""
                        <tr>
                            <td>{col.position}</td>
                            <td><strong>{html.escape(col.name)}</strong></td>
                            <td><span class="badge type-badge">{html.escape(col.data_type)}</span></td>
                            <td>{html.escape(col.size) if col.size else '-'}</td>
                            <td>{nullable_badge}</td>
                            <td>{html.escape(str(col.default_value)) if col.default_value else '-'}</td>
                            <td>{keys_html}</td>
                        </tr>
""")

            html_parts.append("""
                    </tbody>
                </table>
""")

            # Indexes section
            if table.indexes:
                html_parts.append("""
                <div class="index-section">
                    <h3>Indexes</h3>
""")
                for idx in table.indexes:
                    unique_badge = '<span class="badge badge-unique">UNIQUE</span>' if idx.is_unique else ''
                    primary_badge = '<span class="badge badge-pk">PRIMARY</span>' if idx.is_primary else ''
                    
                    html_parts.append(f"""
                    <div class="index-item">
                        <strong>{html.escape(idx.name)}</strong> {unique_badge} {primary_badge}
                        <div style="margin-top: 5px; color: #6b7280;">
                            Columns: {html.escape(', '.join(idx.columns))}
                        </div>
                    </div>
""")
                html_parts.append("</div>")

            # Relationships section
            if table.relationships:
                html_parts.append("""
                <div class="relationship-section">
                    <h3>Relationships</h3>
""")
                for rel in table.relationships:
                    html_parts.append(f"""
                    <div class="relationship-item">
                        <strong>{html.escape(rel.from_column)}</strong> → 
                        <strong>{html.escape(rel.to_table)}.{html.escape(rel.to_column)}</strong>
                    </div>
""")
                html_parts.append("</div>")

            # Sample data section
            if table.sample_data:
                html_parts.append(f"""
                <h3 style="margin-top: 25px;">Sample Data (First {len(table.sample_data)} rows)</h3>
""")
                for i, row in enumerate(table.sample_data, 1):
                    html_parts.append(f"""
                <div class="sample-data">
                    <div class="row-header">Row {i}</div>
""")
                    for key, value in row.items():
                        if value is None:
                            val_html = '<span class="null-value">NULL</span>'
                        else:
                            val_str = str(value)
                            if len(val_str) > 200:
                                val_str = val_str[:197] + "..."
                            val_html = f'<span class="field-value">{html.escape(val_str)}</span>'
                        
                        html_parts.append(f"""
                    <div class="field">
                        <span class="field-name">{html.escape(str(key))}:</span> {val_html}
                    </div>
""")
                    html_parts.append("</div>")

            html_parts.append("""
            </div>
        </div>
""")

        # Footer
        html_parts.append("""
    </div>
</body>
</html>
""")

        with output_path.open("w", encoding="utf-8") as f:
            f.write("".join(html_parts))

    def _generate_markdown_report(self, tables: Dict[str, Table], stats: DatabaseStats, output_path: Path) -> None:
        """Generate a Markdown format report."""
        with output_path.open("w", encoding="utf-8") as f:
            # Header
            f.write("# Database Schema Documentation\n\n")
            f.write(f"**Database:** `{self.db_path.name}`\n")
            f.write(f"**Path:** `{self.db_path.absolute()}`\n")
            f.write(f"**Size:** {self._format_bytes(stats.database_size_bytes)}\n")
            f.write(f"**Generated:** {datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
            f.write(f"**Extraction Time:** {stats.extraction_time_seconds:.2f} seconds\n\n")
            
            # Statistics
            f.write("## Statistics\n\n")
            f.write(f"- **Total Tables:** {stats.total_tables}\n")
            f.write(f"- **Total Columns:** {stats.total_columns}\n")
            f.write(f"- **Total Rows:** {stats.total_rows:,}\n")
            f.write(f"- **Total Indexes:** {stats.total_indexes}\n")
            f.write(f"- **Total Relationships:** {stats.total_relationships}\n\n")
            
            # Table of Contents
            f.write("## Table of Contents\n\n")
            for table_name in sorted(tables.keys()):
                table = tables[table_name]
                f.write(f"- [{table_name}](#{table_name.lower().replace(' ', '-')}) ")
                f.write(f"({table.row_count:,} rows, {len(table.columns)} columns)\n")
            f.write("\n")
            
            # Tables
            f.write("## Tables\n\n")
            for table_name in sorted(tables.keys()):
                table = tables[table_name]
                
                f.write(f"### {table_name}\n\n")
                f.write(f"**Rows:** {table.row_count:,} | ")
                f.write(f"**Columns:** {len(table.columns)} | ")
                f.write(f"**Size:** ~{self._format_bytes(table.estimated_size_bytes)}\n\n")
                
                # Schema table
                f.write("#### Schema\n\n")
                f.write("| # | Column | Type | Size | Nullable | Default | Keys |\n")
                f.write("|---|--------|------|------|----------|---------|------|\n")
                
                for col in table.columns:
                    keys = []
                    if col.is_primary_key:
                        keys.append("PK")
                    if col.is_foreign_key:
                        keys.append("FK")
                    keys_str = ", ".join(keys) if keys else "-"
                    
                    f.write(f"| {col.position} ")
                    f.write(f"| **{col.name}** ")
                    f.write(f"| `{col.data_type}` ")
                    f.write(f"| {col.size if col.size else '-'} ")
                    f.write(f"| {'Yes' if col.nullable else 'No'} ")
                    f.write(f"| {col.default_value if col.default_value else '-'} ")
                    f.write(f"| {keys_str} |\n")
                
                f.write("\n")
                
                # Indexes
                if table.indexes:
                    f.write("#### Indexes\n\n")
                    for idx in table.indexes:
                        f.write(f"- **{idx.name}**")
                        if idx.is_unique:
                            f.write(" (UNIQUE)")
                        if idx.is_primary:
                            f.write(" (PRIMARY)")
                        f.write(f"\n  - Columns: {', '.join(idx.columns)}\n")
                    f.write("\n")
                
                # Relationships
                if table.relationships:
                    f.write("#### Relationships\n\n")
                    for rel in table.relationships:
                        f.write(f"- {rel.from_column} → {rel.to_table}.{rel.to_column}\n")
                    f.write("\n")
                
                # Sample data
                if table.sample_data:
                    f.write(f"#### Sample Data (First {len(table.sample_data)} rows)\n\n")
                    
                    # Create markdown table for sample data
                    if table.sample_data:
                        headers = list(table.sample_data[0].keys())
                        f.write("| " + " | ".join(headers) + " |\n")
                        f.write("|" + "---|" * len(headers) + "\n")
                        
                        for row in table.sample_data:
                            values = []
                            for header in headers:
                                val = row.get(header)
                                if val is None:
                                    values.append("_NULL_")
                                else:
                                    val_str = str(val)
                                    if len(val_str) > 50:
                                        val_str = val_str[:47] + "..."
                                    # Escape pipe characters in markdown
                                    val_str = val_str.replace("|", "\\|")
                                    values.append(val_str)
                            f.write("| " + " | ".join(values) + " |\n")
                
                f.write("\n---\n\n")


def main():
    """Main entry point with argument parsing."""
    parser = argparse.ArgumentParser(
        description="Extract comprehensive schema information from MS Access databases",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  %(prog)s database.mdb                    # Basic text report
  %(prog)s database.mdb -f html           # HTML report with styling
  %(prog)s database.mdb -f json -s 10     # JSON with 10 sample rows
  %(prog)s database.mdb -f markdown       # Markdown documentation
        """
    )

    parser.add_argument(
        "database",
        help="Path to the Access database file (.mdb or .accdb)"
    )

    parser.add_argument(
        "-o", "--output",
        help="Output file path (default: auto-generated based on format)"
    )

    parser.add_argument(
        "-f", "--format",
        choices=["text", "json", "html", "markdown"],
        default="text",
        help="Output format (default: text)"
    )

    parser.add_argument(
        "-s", "--samples",
        type=int,
        default=5,
        help="Number of sample rows to extract per table (default: 5, use 0 for none)"
    )

    parser.add_argument(
        "-t", "--timeout",
        type=int,
        default=30,
        help="Command timeout in seconds (default: 30)"
    )

    parser.add_argument(
        "-v", "--verbose",
        action="store_true",
        help="Enable verbose/debug logging"
    )

    parser.add_argument(
        "--version",
        action="version",
        version="%(prog)s 2.0.0"
    )

    args = parser.parse_args()

    # Configure logging level
    if args.verbose:
        logger.setLevel(logging.DEBUG)

    # Auto-generate output filename if not specified
    if not args.output:
        db_name = Path(args.database).stem
        timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
        ext_map = {
            "text": ".txt",
            "json": ".json",
            "html": ".html",
            "markdown": ".md"
        }
        args.output = f"{db_name}_schema_{timestamp}{ext_map[args.format]}"

    try:
        print(f"🔍 Analyzing database: {args.database}")
        
        # Create extractor and generate report
        extractor = DatabaseSchemaExtractor(
            args.database,
            sample_rows=args.samples,
            timeout=args.timeout
        )
        
        extractor.generate_report(args.output, args.format)
        
        print(f"✅ Success! Report saved to: {args.output}")
        
        # Open HTML reports in browser if available
        if args.format == "html":
            try:
                import webbrowser
                webbrowser.open(f"file://{Path(args.output).absolute()}")
                print("📊 Opening report in browser...")
            except Exception:
                pass

    except FileNotFoundError as e:
        print(f"❌ Error: {e}")
        sys.exit(1)
    except EnvironmentError as e:
        print(f"❌ Environment Error: {e}")
        print("\n💡 Tip: Make sure mdbtools is installed:")
        print("  Ubuntu/Debian: sudo apt-get install mdbtools")
        print("  macOS: brew install mdbtools")
        sys.exit(1)
    except KeyboardInterrupt:
        print("\n⚠️ Operation cancelled by user")
        sys.exit(130)
    except Exception as e:
        logger.exception("Unexpected error occurred")
        print(f"❌ Unexpected error: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()