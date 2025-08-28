#!/usr/bin/env python3
import shutil
import sys
import os

def reset_db():
    """Copies the clean template DB over the active one."""
    template_path = "db/template.accdb"
    active_path = "db/OrphanageHub.accdb"
    if not os.path.exists(template_path):
        print(f"ERROR: Template database '{template_path}' not found!")
        sys.exit(1)
    try:
        shutil.copyfile(template_path, active_path)
        print("✅ Database successfully reset from template.")
    except Exception as e:
        print(f"❌ ERROR: Could not reset database: {e}")
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) > 1 and sys.argv[1] == "reset":
        reset_db()
    else:
        print("Usage: python db_manager.py reset")
