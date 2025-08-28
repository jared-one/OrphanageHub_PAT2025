#!/usr/bin/env python3
"""
Refactors all Java files to use SLF4J logging instead of custom Logger.
"""
import os
import re
from pathlib import Path

def refactor_java_file(filepath):
    """Refactor a single Java file to use SLF4J logging."""
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    original_content = content
    
    # Extract class name
    class_match = re.search(r'public\s+(?:final\s+)?(?:abstract\s+)?class\s+(\w+)', content)
    if not class_match:
        class_match = re.search(r'public\s+interface\s+(\w+)', content)
    if not class_match:
        print(f"  ⚠️  Could not find class name in {filepath}")
        return False
    
    class_name = class_match.group(1)
    
    # Remove old Logger import
    content = re.sub(r'import\s+com\.orphanagehub\.util\.Logger\s*;\s*\n?', '', content)
    
    # Check if SLF4J imports already exist
    has_slf4j = 'import org.slf4j.Logger;' in content
    
    if not has_slf4j:
        # Add SLF4J imports after package declaration
        package_match = re.search(r'(package\s+[\w\.]+\s*;)', content)
        if package_match:
            package_line = package_match.group(1)
            new_imports = f"{package_line}\n\nimport org.slf4j.Logger;\nimport org.slf4j.LoggerFactory;"
            content = content.replace(package_line, new_imports, 1)
    
    # Check if logger field already exists
    has_logger_field = re.search(r'private\s+static\s+final\s+Logger\s+logger\s*=', content)
    
    if not has_logger_field:
        # Add logger field after class declaration
        class_pattern = r'(public\s+(?:final\s+)?(?:abstract\s+)?class\s+' + class_name + r'[^{]*\{)'
        class_match = re.search(class_pattern, content)
        if class_match:
            class_declaration = class_match.group(1)
            logger_field = f"{class_declaration}\n    private static final Logger logger = LoggerFactory.getLogger({class_name}.class);\n"
            content = content.replace(class_declaration, logger_field, 1)
    
    # Replace static Logger calls with instance logger calls
    # Logger.info(...) -> logger.info(...)
    content = re.sub(r'\bLogger\.(info|debug|warn|error|trace)\s*KATEX_INLINE_OPEN', r'logger.\1(', content)
    
    # Update string concatenation to parameterized logging where possible
    # Simple case: "text" + variable -> "text {}", variable
    content = re.sub(
        r'logger\.(info|debug|warn|error)\s*KATEX_INLINE_OPEN\s*"([^"]+)"\s*\+\s*([^)]+)\s*KATEX_INLINE_CLOSE',
        r'logger.\1("\2 {}", \3)',
        content
    )
    
    if content != original_content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"  ✅ Refactored: {filepath}")
        return True
    else:
        print(f"  ⏭️  No changes needed: {filepath}")
        return False

def find_java_files(root_dir):
    """Find all Java files in the project."""
    java_files = []
    for root, dirs, files in os.walk(root_dir):
        # Skip test and target directories
        if 'target' in root or 'test' in root:
            continue
        for file in files:
            if file.endswith('.java'):
                java_files.append(os.path.join(root, file))
    return java_files

def main():
    print("🔧 Starting Java logging refactoring...")
    
    src_dir = 'src/main/java'
    if not os.path.exists(src_dir):
        print(f"❌ Source directory '{src_dir}' not found!")
        return
    
    java_files = find_java_files(src_dir)
    print(f"📁 Found {len(java_files)} Java files to process\n")
    
    refactored_count = 0
    for filepath in java_files:
        if refactor_java_file(filepath):
            refactored_count += 1
    
    print(f"\n✨ Refactoring complete! Modified {refactored_count} files.")

if __name__ == "__main__":
    main()
