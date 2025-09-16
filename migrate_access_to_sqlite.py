#!/usr/bin/env python3
"""
Migration: MS Access (.mdb/.accdb) -> SQLite
Fixed version: handles duplicate columns and ensures table creation before data import.

Usage:
    python3 migrate_access_to_sqlite.py path/to/source.accdb path/to/target.sqlite

Requires: mdbtools (mdb-tables, mdb-export, mdb-schema). Optional: tqdm, colorlog
"""
from __future__ import annotations

import argparse
import csv
import datetime
import logging
import os
import re
import shutil
import sqlite3
import subprocess
import sys
import tempfile
from dataclasses import dataclass
from datetime import datetime as dt
from enum import Enum
from pathlib import Path
from typing import Any, Dict, Iterator, List, Optional, Tuple

# Optional nice-to-have libraries
try:
    from tqdm import tqdm as _tqdm
    TQDM_AVAILABLE = True
except Exception:
    _tqdm = None
    TQDM_AVAILABLE = False

try:
    import colorlog
    handler = colorlog.StreamHandler()
    handler.setFormatter(colorlog.ColoredFormatter('%(log_color)s%(asctime)s - %(levelname)s - %(message)s', datefmt='%Y-%m-%d %H:%M:%S'))
    logger = colorlog.getLogger(__name__)
    logger.addHandler(handler)
    logger.setLevel(logging.INFO)
except Exception:
    logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s', datefmt='%Y-%m-%d %H:%M:%S')
    logger = logging.getLogger(__name__)


class NullPBar:
    """Minimal no-op progress bar with same public surface used in this script."""
    def __init__(self, total: int = 0, desc: str = '', unit: str = 'it'):
        self.total = total
        self.n = 0
        self.desc = desc
        self.unit = unit
    def update(self, n: int = 1):
        self.n += n
    def close(self):
        return
    def refresh(self):
        return


class DataType(Enum):
    INTEGER = "INTEGER"
    REAL = "REAL"
    TEXT = "TEXT"
    BLOB = "BLOB"
    DATETIME = "DATETIME"
    BOOLEAN = "BOOLEAN"


@dataclass
class Column:
    name: str
    original_name: str
    access_type: str
    sqlite_type: str
    is_primary_key: bool = False
    is_nullable: bool = True
    default_value: Optional[str] = None
    max_length: Optional[int] = None


@dataclass
class Table:
    name: str
    columns: List[Column]
    row_count: int = 0
    primary_keys: Optional[List[str]] = None
    sample_data: Optional[List[Dict[str, Any]]] = None


@dataclass
class MigrationStats:
    start_time: dt
    end_time: Optional[dt] = None
    tables_migrated: int = 0
    total_rows: int = 0
    total_columns: int = 0
    errors: List[str] = None
    warnings: List[str] = None
    source_size: int = 0
    target_size: int = 0
    verification_passed: bool = False

    def __post_init__(self):
        if self.errors is None:
            self.errors = []
        if self.warnings is None:
            self.warnings = []

    @property
    def duration(self) -> float:
        if self.end_time:
            return (self.end_time - self.start_time).total_seconds()
        return 0.0


class AccessToSQLiteMigrator:
    TYPE_MAPPING = {
        'text': DataType.TEXT,
        'memo': DataType.TEXT,
        'varchar': DataType.TEXT,
        'char': DataType.TEXT,
        'byte': DataType.INTEGER,
        'integer': DataType.INTEGER,
        'long': DataType.INTEGER,
        'autoincrement': DataType.INTEGER,
        'counter': DataType.INTEGER,
        'single': DataType.REAL,
        'double': DataType.REAL,
        'float': DataType.REAL,
        'currency': DataType.REAL,
        'decimal': DataType.REAL,
        'datetime': DataType.DATETIME,
        'date': DataType.DATETIME,
        'time': DataType.DATETIME,
        'timestamp': DataType.DATETIME,
        'yes/no': DataType.BOOLEAN,
        'boolean': DataType.BOOLEAN,
        'bit': DataType.INTEGER,
        'ole': DataType.BLOB,
        'attachment': DataType.BLOB,
        'hyperlink': DataType.TEXT,
        'autonumber': DataType.INTEGER,
    }

    def __init__(self,
                 access_db: str,
                 sqlite_db: str,
                 batch_size: int = 1000,
                 preserve_types: bool = True,
                 create_indexes: bool = True,
                 verify_data: bool = True,
                 backup_existing: bool = True,
                 sample_for_types: int = 200):
        self.access_db = Path(access_db)
        self.sqlite_db = Path(sqlite_db)
        self.batch_size = max(100, int(batch_size))
        self.preserve_types = bool(preserve_types)
        self.create_indexes = bool(create_indexes)
        self.verify_data = bool(verify_data)
        self.backup_existing = bool(backup_existing)
        self.sample_for_types = int(sample_for_types)
        self.stats = MigrationStats(start_time=dt.now())
        self.tables: Dict[str, Table] = {}
        self.tables_created: Dict[str, bool] = {}  # Track which tables were successfully created
        self._validate_environment()

    def _format_bytes(self, size: int) -> str:
        for unit in ['B', 'KB', 'MB', 'GB']:
            if size < 1024.0:
                return f"{size:.2f} {unit}"
            size /= 1024.0
        return f"{size:.2f} TB"

    def _validate_environment(self):
        if not self.access_db.exists():
            raise FileNotFoundError(f"Access DB not found: {self.access_db}")
        if self.access_db.suffix.lower() not in ['.mdb', '.accdb']:
            raise ValueError(f"Invalid Access DB extension: {self.access_db.suffix}")

        for tool in ('mdb-tables', 'mdb-export', 'mdb-schema'):
            if shutil.which(tool) is None:
                raise EnvironmentError(f"Required tool '{tool}' not found. Install mdbtools.")

        self.sqlite_db.parent.mkdir(parents=True, exist_ok=True)
        if self.backup_existing and self.sqlite_db.exists():
            bak = self.sqlite_db.with_suffix(f'.backup_{dt.now():%Y%m%d_%H%M%S}.db')
            shutil.copy2(self.sqlite_db, bak)
            logger.info(f"Backed up existing DB to: {bak}")

        self.stats.source_size = self.access_db.stat().st_size
        logger.info(f"Source DB size: {self._format_bytes(self.stats.source_size)}")

    def _run_cmd(self, cmd: List[str], timeout: int = 60) -> str:
        logger.debug("Run cmd: %s", ' '.join(cmd))
        cp = subprocess.run(cmd, capture_output=True, text=True, check=False, timeout=timeout)
        if cp.returncode != 0:
            logger.debug("Cmd stderr: %s", cp.stderr)
            raise subprocess.CalledProcessError(cp.returncode, cmd, cp.stderr)
        return cp.stdout

    def _stream_cmd(self, cmd: List[str]) -> Iterator[str]:
        logger.debug("Stream cmd: %s", ' '.join(cmd))
        p = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
        assert p.stdout is not None
        try:
            for line in p.stdout:
                yield line.rstrip('\n')
            p.wait()
            if p.returncode != 0:
                stderr = p.stderr.read() if p.stderr is not None else ''
                raise subprocess.CalledProcessError(p.returncode, cmd, stderr)
        finally:
            if p.stdout:
                p.stdout.close()
            if p.stderr:
                p.stderr.close()

    def _sanitize_column_name(self, name: str, position: int) -> str:
        name = (name or '').strip()
        name = name.strip("()[]'\"`")
        if not name:
            name = f"column_{position}"
        name = re.sub(r"[^0-9A-Za-z_]", '_', name)
        if name and name[0].isdigit():
            name = f"col_{name}"
        reserved = {'order','group','table','index','select','from','where','join','union','create','drop','alter','insert','update'}
        if name.lower() in reserved:
            name = f"{name}_field"
        return name

    def _get_tables(self) -> List[str]:
        out = self._run_cmd(['mdb-tables', '-1', str(self.access_db)])
        tables = [t.strip() for t in out.splitlines() if t.strip() and not t.strip().startswith('MSys')]
        logger.info(f"Found {len(tables)} user tables")
        return sorted(tables)

    def _detect_column_type(self, col_name: str, schema_text: str) -> str:
        detected = 'Text'
        for line in schema_text.splitlines():
            if col_name in line or f'[{col_name}]' in line:
                ll = line.lower()
                if 'memo' in ll:
                    detected = 'Memo'
                elif 'long' in ll or 'integer' in ll or 'counter' in ll:
                    detected = 'Long'
                elif 'datetime' in ll or 'date' in ll or 'time' in ll:
                    detected = 'DateTime'
                elif 'currency' in ll or 'money' in ll:
                    detected = 'Currency'
                elif 'double' in ll or 'float' in ll:
                    detected = 'Double'
                elif 'yes/no' in ll or 'boolean' in ll:
                    detected = 'Yes/No'
                break
        return detected

    def _map_to_sqlite(self, access_type: str) -> str:
        if not self.preserve_types:
            return DataType.TEXT.value
        al = (access_type or '').lower()
        for pat, dtv in self.TYPE_MAPPING.items():
            if pat.strip() and pat in al:
                return dtv.value
        return DataType.TEXT.value

    def _is_likely_pk(self, column_name: str, table_name: str) -> bool:
        c = column_name.lower()
        t = table_name.lower()
        if c == 'id':
            return True
        if c.endswith('id') and c.startswith(t[:3]):
            return True
        if c == f"{t}_id" or c == f"{t}id":
            return True
        if c in ('pk', 'primary_key', 'key'):
            return True
        return False

    def _analyze_table(self, table_name: str) -> Table:
        stream = self._stream_cmd(['mdb-export', '-H', str(self.access_db), table_name])
        header = next(stream, None)
        samples = []
        for _ in range(10):
            ln = next(stream, None)
            if ln is None:
                break
            samples.append(ln)

        if not header:
            logger.warning(f"Empty table: {table_name}")
            return Table(name=table_name, columns=[], row_count=0)

        hdr = next(csv.reader([header]))
        schema_text = self._run_cmd(['mdb-schema', str(self.access_db), '-T', table_name])

        # sanitize & deduplicate column names - FIXED VERSION
        seen = {}
        seen_lower = {}  # Track lowercase versions to avoid case-insensitive duplicates
        columns: List[Column] = []
        
        for i, orig in enumerate(hdr, start=1):
            base = self._sanitize_column_name(orig, i)
            base_lower = base.lower()
            
            # Check if we've seen this name (case-insensitive)
            if base_lower in seen_lower:
                # Add numeric suffix to make unique
                suffix = 2
                while f"{base_lower}_{suffix}" in seen_lower:
                    suffix += 1
                name = f"{base}_{suffix}"
                name_lower = f"{base_lower}_{suffix}"
            else:
                name = base
                name_lower = base_lower
            
            seen[name] = True
            seen_lower[name_lower] = True

            access_type = self._detect_column_type(orig, schema_text)
            sqlite_type = self._map_to_sqlite(access_type)
            is_pk = self._is_likely_pk(name, table_name)
            columns.append(Column(
                name=name, 
                original_name=orig, 
                access_type=access_type, 
                sqlite_type=sqlite_type, 
                is_primary_key=is_pk, 
                is_nullable=not is_pk
            ))

        # row count (best-effort)
        try:
            full = self._run_cmd(['mdb-export', '-H', str(self.access_db), table_name])
            row_count = max(0, len(full.splitlines()) - 1)
        except Exception:
            row_count = len(samples)

        sample_data: List[Dict[str, Any]] = []
        if samples:
            dr = csv.DictReader([header] + samples)
            for r in dr:
                if r:  # Skip empty rows
                    sample_data.append(dict(r))

        return Table(name=table_name, columns=columns, row_count=row_count, 
                    primary_keys=[c.name for c in columns if c.is_primary_key], 
                    sample_data=sample_data)

    def _create_table_sql(self, table: Table) -> str:
        # Ensure no duplicate column names in the final SQL
        seen_cols = set()
        cols = []
        
        for col in table.columns:
            if not col.name or col.name in seen_cols:
                continue
            seen_cols.add(col.name)
            
            col_def = f'"{col.name}" {col.sqlite_type}'
            if col.is_primary_key and len([c for c in table.columns if c.is_primary_key]) == 1:
                col_def += ' PRIMARY KEY'
            elif not col.is_nullable:
                col_def += ' NOT NULL'
            if col.default_value:
                col_def += f' DEFAULT {col.default_value}'
            cols.append(col_def)
        
        if not cols:
            raise ValueError(f"No valid columns for table {table.name}")
            
        return f'CREATE TABLE IF NOT EXISTS "{table.name}" ({", ".join(cols)})'

    def _export_table_to_temp_csv(self, table_name: str) -> Path:
        tmp = tempfile.NamedTemporaryFile(mode='w+', suffix='.csv', delete=False, encoding='utf-8', newline='')
        tmp.close()
        path = Path(tmp.name)
        out = self._run_cmd(['mdb-export', '-H', '-d', ',', '-q', '"', '-D', '%Y-%m-%d %H:%M:%S', str(self.access_db), table_name])
        path.write_text(out, encoding='utf-8')
        return path

    def _convert_row_dict(self, row: Dict[str, str], table: Table) -> Dict[str, Any]:
        out: Dict[str, Any] = {}
        for col in table.columns:
            val = None
            # try original header first, then sanitized name
            if col.original_name in row:
                val = row.get(col.original_name)
            elif col.name in row:
                val = row.get(col.name)
            else:
                # try variants
                try_clean = col.original_name.strip("()[]'\"`") if col.original_name else None
                if try_clean and try_clean in row:
                    val = row.get(try_clean)

            if val in ('', 'NULL', None):
                out[col.name] = None
                continue
            try:
                if col.sqlite_type == DataType.INTEGER.value:
                    if col.access_type.lower() in ('yes/no', 'boolean'):
                        out[col.name] = 1 if str(val).lower() in ('1', 'true', 'yes') else 0
                    else:
                        out[col.name] = int(val)
                elif col.sqlite_type == DataType.REAL.value:
                    out[col.name] = float(val)
                else:
                    out[col.name] = val
            except Exception:
                out[col.name] = val
        return out

    def _import_csv_into_table(self, conn: sqlite3.Connection, table: Table, csv_path: Path, pbar=None) -> int:
        with open(csv_path, 'r', encoding='utf-8', newline='') as fh:
            reader = csv.DictReader(fh)
            valid_cols = [c.name for c in table.columns if c.name]
            if not valid_cols:
                logger.warning(f"No valid columns for {table.name}")
                return 0
            placeholders = ','.join(['?'] * len(valid_cols))
            quoted_cols = ','.join([f'"{c}"' for c in valid_cols])
            insert_sql = f'INSERT INTO "{table.name}" ({quoted_cols}) VALUES ({placeholders})'

            batch: List[Tuple] = []
            total = 0
            for i, row in enumerate(reader, start=1):
                try:
                    conv = self._convert_row_dict(row, table)
                    vals = [conv.get(c) for c in valid_cols]
                    batch.append(tuple(vals))
                    if len(batch) >= self.batch_size:
                        conn.executemany(insert_sql, batch)
                        total += len(batch)
                        if pbar:
                            pbar.update(len(batch))
                        batch = []
                except Exception as e:
                    logger.warning(f"Row import error {table.name}#{i}: {e}")
                    self.stats.warnings.append(f"{table.name} row {i}: {e}")
            if batch:
                conn.executemany(insert_sql, batch)
                total += len(batch)
                if pbar:
                    pbar.update(len(batch))
        logger.info(f"Imported {total} rows into {table.name}")
        return total

    def _create_indexes(self, conn: sqlite3.Connection):
        if not self.create_indexes:
            return
        logger.info("Creating indexes...")
        for table_name, table in self.tables.items():
            # Only create indexes for successfully created tables
            if not self.tables_created.get(table_name, False):
                continue
                
            for col in table.columns:
                cl = col.name.lower()
                if cl.endswith('id') and not col.is_primary_key:
                    idx = f'idx_{table.name}_{col.name}'
                    try:
                        conn.execute(f'CREATE INDEX IF NOT EXISTS "{idx}" ON "{table.name}" ("{col.name}")')
                    except Exception as e:
                        logger.warning(f"Index creation failed {idx}: {e}")

    def _verify(self, conn: sqlite3.Connection) -> bool:
        if not self.verify_data:
            return True
        logger.info("Verifying migration...")
        ok = True
        for name, table in self.tables.items():
            # Only verify successfully created tables
            if not self.tables_created.get(name, False):
                continue
                
            try:
                cur = conn.execute(f'SELECT COUNT(*) FROM "{name}"')
                cnt = cur.fetchone()[0]
                if cnt != table.row_count:
                    logger.warning(f"Row count mismatch {name}: expected {table.row_count}, got {cnt}")
                    self.stats.warnings.append(f"{name}: {cnt}/{table.row_count}")
                    if abs(cnt - table.row_count) > max(1, int(table.row_count * 0.1)):
                        ok = False
            except Exception as e:
                logger.error(f"Verification failed for {name}: {e}")
                self.stats.errors.append(f"Verification error: {name}")
                ok = False
        return ok

    def _generate_report(self) -> str:
        self.stats.end_time = self.stats.end_time or dt.now()
        if self.sqlite_db.exists():
            self.stats.target_size = self.sqlite_db.stat().st_size
        lines: List[str] = []
        lines.append("=" * 80)
        lines.append("ACCESS -> SQLITE MIGRATION REPORT")
        lines.append("=" * 80)
        lines.append("")
        lines.append(f"Source: {self.access_db}")
        lines.append(f"Target: {self.sqlite_db}")
        lines.append(f"Started: {self.stats.start_time:%Y-%m-%d %H:%M:%S}")
        lines.append(f"Completed: {self.stats.end_time:%Y-%m-%d %H:%M:%S}")
        lines.append(f"Duration: {self.stats.duration:.2f} seconds")
        lines.append(f"Source Size: {self._format_bytes(self.stats.source_size)}")
        lines.append(f"Target Size: {self._format_bytes(self.stats.target_size)}")
        lines.append("")
        lines.append("Tables:")
        for t in self.tables.values():
            status = "✓" if self.tables_created.get(t.name, False) else "✗"
            lines.append(f" {status} {t.name}: {t.row_count} rows, {len(t.columns)} cols")
        lines.append("")
        if self.stats.warnings:
            lines.append("WARNINGS:")
            for w in self.stats.warnings:
                lines.append(f" - {w}")
            lines.append("")
        if self.stats.errors:
            lines.append("ERRORS:")
            for e in self.stats.errors:
                lines.append(f" - {e}")
            lines.append("")
        return "\n".join(lines)

    def migrate(self) -> bool:
        logger.info(f"Starting migration: {self.access_db} -> {self.sqlite_db}")
        table_names = self._get_tables()
        if not table_names:
            logger.error("No tables found")
            return False

        logger.info("Analyzing tables...")
        for tn in table_names:
            try:
                tbl = self._analyze_table(tn)
                self.tables[tn] = tbl
                self.stats.total_columns += len(tbl.columns)
            except Exception as e:
                logger.error(f"Schema analysis failed for {tn}: {e}")
                self.stats.errors.append(f"Schema analysis failed: {tn}")

        conn = sqlite3.connect(str(self.sqlite_db))
        conn.execute('PRAGMA foreign_keys = ON')
        conn.execute('PRAGMA journal_mode = WAL')
        conn.execute('PRAGMA synchronous = NORMAL')
        conn.execute('PRAGMA cache_size = -64000')
        conn.execute('PRAGMA temp_store = MEMORY')

        try:
            # Create tables and track success
            for t in self.tables.values():
                try:
                    create_sql = self._create_table_sql(t)
                    conn.execute(f'DROP TABLE IF EXISTS "{t.name}"')
                    conn.execute(create_sql)
                    self.tables_created[t.name] = True
                    logger.info(f"Created table: {t.name}")
                except Exception as e:
                    logger.error(f"Failed creating table {t.name}: {e}")
                    self.stats.errors.append(f"Table creation failed: {t.name}: {str(e)}")
                    self.tables_created[t.name] = False

            # Calculate total rows only for successfully created tables
            total_rows = sum(t.row_count for t in self.tables.values() 
                           if self.tables_created.get(t.name, False))
            
            if total_rows == 0:
                logger.warning("No data to import (no tables created successfully)")
            else:
                logger.info(f"Beginning data import ({total_rows} rows)")
                
            pbar = _tqdm(total=total_rows, desc='Migrating rows', unit='rows') if TQDM_AVAILABLE else NullPBar(total=total_rows, desc='Migrating rows', unit='rows')

            for t in self.tables.values():
                # Skip tables that weren't created successfully
                if not self.tables_created.get(t.name, False):
                    logger.warning(f"Skipping data import for {t.name} (table creation failed)")
                    continue
                    
                if t.row_count == 0:
                    continue
                    
                logger.info(f"Exporting table {t.name}...")
                csv_path = None
                try:
                    csv_path = self._export_table_to_temp_csv(t.name)
                    inserted = self._import_csv_into_table(conn, t, csv_path, pbar)
                    self.stats.total_rows += inserted
                    self.stats.tables_migrated += 1
                except Exception as e:
                    logger.error(f"Failed to import data for {t.name}: {e}")
                    self.stats.errors.append(f"Data import failed: {t.name}: {str(e)}")
                finally:
                    if csv_path:
                        try:
                            csv_path.unlink()
                        except Exception:
                            pass

            if hasattr(pbar, 'close'):
                try:
                    pbar.close()
                except Exception:
                    pass

            self._create_indexes(conn)
            conn.commit()

            self.stats.verification_passed = self._verify(conn)

            self.stats.end_time = dt.now()
            if self.sqlite_db.exists():
                self.stats.target_size = self.sqlite_db.stat().st_size

            report = self._generate_report()
            rp = self.sqlite_db.with_suffix('.migration_report.txt')
            rp.write_text(report)
            print("\n" + report)
            print(f"\nFull report saved to: {rp}")

            success = self.stats.verification_passed and not self.stats.errors
            if success:
                logger.info("✅ Migration completed successfully!")
            else:
                logger.warning("⚠️ Migration completed with warnings/errors")
            return success

        except Exception as e:
            logger.exception("Migration failed")
            conn.rollback()
            self.stats.errors.append(str(e))
            return False
        finally:
            conn.close()


def parse_args() -> argparse.Namespace:
    p = argparse.ArgumentParser(description='Migrate MS Access (.mdb/.accdb) to SQLite')
    p.add_argument('access_db')
    p.add_argument('sqlite_db')
    p.add_argument('--batch-size', type=int, default=1000)
    p.add_argument('--no-types', action='store_true')
    p.add_argument('--no-indexes', action='store_true')
    p.add_argument('--no-verify', action='store_true')
    p.add_argument('--no-backup', action='store_true')
    p.add_argument('--samples', type=int, default=200, help='Sample rows used for type inference')
    p.add_argument('-v', '--verbose', action='store_true')
    return p.parse_args()


def main() -> None:
    args = parse_args()
    if args.verbose:
        logger.setLevel(logging.DEBUG)

    migrator = AccessToSQLiteMigrator(
        access_db=args.access_db,
        sqlite_db=args.sqlite_db,
        batch_size=args.batch_size,
        preserve_types=not args.no_types,
        create_indexes=not args.no_indexes,
        verify_data=not args.no_verify,
        backup_existing=not args.no_backup,
        sample_for_types=args.samples
    )

    ok = migrator.migrate()
    sys.exit(0 if ok else 1)


if __name__ == '__main__':
    main()