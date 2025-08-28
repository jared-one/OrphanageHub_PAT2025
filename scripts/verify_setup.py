#!/usr/bin/env python3
"""Verify that the project setup is complete and correct."""

import os
import sys
from pathlib import Path

def check_item(description, condition, fix_hint=""):
    """Check a single configuration item."""
    if condition:
        print(f"✅ {description}")
        return True
    else:
        print(f"❌ {description}")
        if fix_hint:
            print(f"   Fix: {fix_hint}")
        return False

def main():
    print("🔍 Verifying OrphanageHub Setup")
    print("=" * 40)
    
    all_good = True
    
    # Check directories
    all_good &= check_item(
        "logs/ directory exists",
        os.path.exists("logs"),
        "Run: mkdir -p logs"
    )
    
    all_good &= check_item(
        "db/ directory exists",
        os.path.exists("db"),
        "Run: mkdir -p db"
    )
    
    # Check database files
    all_good &= check_item(
        "Main database exists",
        os.path.exists("db/OrphanageHub.accdb"),
        "Copy your Access database to db/OrphanageHub.accdb"
    )
    
    all_good &= check_item(
        "Template database exists",
        os.path.exists("db/template.accdb"),
        "Run: cp db/OrphanageHub.accdb db/template.accdb"
    )
    
    # Check configuration files
    all_good &= check_item(
        "app.properties exists",
        os.path.exists("src/main/resources/app.properties"),
        "Create src/main/resources/app.properties with db.path=db/OrphanageHub.accdb"
    )
    
    all_good &= check_item(
        "logback.xml exists",
        os.path.exists("src/main/resources/logback.xml"),
        "Logback configuration missing"
    )
    
    # Check Python scripts
    all_good &= check_item(
        "Python scripts are executable",
        os.access("scripts/db_manager.py", os.X_OK),
        "Run: chmod +x scripts/*.py"
    )
    
    # Check Java source files
    java_files = list(Path("src/main/java").rglob("*.java"))
    all_good &= check_item(
        f"Java source files found ({len(java_files)} files)",
        len(java_files) > 0,
        "No Java files found in src/main/java"
    )
    
    # Check for SLF4J usage
    slf4j_files = 0
    for java_file in java_files[:5]:  # Check first 5 files
        with open(java_file, 'r') as f:
            if 'LoggerFactory' in f.read():
                slf4j_files += 1
    
    all_good &= check_item(
        f"SLF4J logging configured ({slf4j_files}/5 files checked)",
        slf4j_files > 0,
        "Run: python3 scripts/refactor_logging.py"
    )
    
    print("=" * 40)
    if all_good:
        print("✨ All checks passed! Your project is ready.")
    else:
        print("⚠️  Some issues found. Please fix them before proceeding.")
        sys.exit(1)

if __name__ == "__main__":
    main()
