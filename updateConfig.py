#!/usr/bin/env python3
"""
Ultimate Configuration Generator for OrphanageHub
Version 2.0 - No syntax issues guaranteed!
"""

import os
import sys
import subprocess
import datetime
from pathlib import Path
import glob

class ConfigGenerator:
    def __init__(self):
        self.output_file = "Configuration.txt"
        self.content = []
        self.stats = {}
        
    def run_command(self, cmd):
        """Run a shell command and return output"""
        try:
            result = subprocess.run(cmd, shell=True, capture_output=True, text=True, timeout=5)
            return result.stdout.strip() if result.returncode == 0 else ""
        except:
            return ""
    
    def add_line(self, text=""):
        """Add a line to content"""
        self.content.append(str(text))
    
    def add_section(self, title):
        """Add a section header"""
        self.add_line("")
        self.add_line("=" * 80)
        self.add_line("## " + title)
        self.add_line("=" * 80)
    
    def add_file(self, filepath, language=""):
        """Add file content"""
        if os.path.exists(filepath):
            self.add_line("")
            self.add_line("### File: " + filepath)
            self.add_line("```" + language)
            try:
                with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
                    self.add_line(f.read())
            except Exception as e:
                self.add_line("Error reading file: " + str(e))
            self.add_line("```")
            return True
        return False
    
    def generate(self):
        """Main generation method"""
        print("🚀 Generating Configuration...")
        
        # Header
        self.add_line("# 🚀 ORPHANAGEHUB PROJECT CONFIGURATION")
        self.add_line("Generated: " + datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S'))
        self.add_line("Working Directory: " + os.getcwd())
        self.add_line("Python Version: " + sys.version.split()[0])
        
        java_version = self.run_command('java -version 2>&1 | head -n 1')
        if java_version:
            self.add_line("Java Version: " + java_version)
        
        maven_version = self.run_command('mvn -version 2>&1 | head -n 1')
        if maven_version:
            self.add_line("Maven Version: " + maven_version)
        
        # 1. Project Structure
        self.add_section("1. PROJECT STRUCTURE")
        self.add_line("```")
        tree_output = self.run_command('tree -I "target|.git|*.class|*.jar" -L 3')
        if tree_output:
            self.add_line(tree_output)
        else:
            # Fallback - list directories
            for root, dirs, files in os.walk(".", topdown=True):
                dirs[:] = [d for d in dirs if d not in ['.git', 'target', '__pycache__']]
                level = root.replace(".", "", 1).count(os.sep)
                if level < 3:
                    indent = "  " * level
                    self.add_line(indent + os.path.basename(root) + "/")
                    if level < 2:
                        for f in files[:5]:
                            if f.endswith(('.java', '.xml', '.properties')):
                                self.add_line(indent + "  " + f)
        self.add_line("```")
        
        # 2. Build Configuration
        self.add_section("2. BUILD CONFIGURATION")
        self.add_file("pom.xml", "xml")
        self.add_file("Makefile", "makefile")
        
        # 3. Java Source Code
        self.add_section("3. JAVA SOURCE CODE")
        
        java_files = []
        for root, dirs, files in os.walk("src"):
            for file in files:
                if file.endswith(".java"):
                    java_files.append(os.path.join(root, file))
        
        java_files.sort()
        self.stats['java_files'] = len(java_files)
        
        self.add_line("")
        self.add_line("### Found " + str(len(java_files)) + " Java files:")
        self.add_line("```")
        for jf in java_files:
            self.add_line(jf)
        self.add_line("```")
        
        # Add each Java file
        total_lines = 0
        for java_file in java_files:
            self.add_line("")
            self.add_line("-" * 60)
            if os.path.exists(java_file):
                self.add_file(java_file, "java")
                try:
                    with open(java_file, 'r') as f:
                        total_lines += len(f.readlines())
                except:
                    pass
        
        self.stats['total_lines'] = total_lines
        
        # 4. Resources
        self.add_section("4. RESOURCES")
        
        # Properties files
        for prop_file in glob.glob("src/main/resources/**/*.properties", recursive=True):
            self.add_file(prop_file, "properties")
        
        # App properties specifically
        self.add_file("src/main/resources/app.properties", "properties")
        
        # 5. Database
        self.add_section("5. DATABASE CONFIGURATION")
        
        self.add_file("db/details_database.txt", "text")
        
        self.add_line("")
        self.add_line("### Database Files:")
        self.add_line("```")
        for db_file in glob.glob("db/*.accdb"):
            if os.path.exists(db_file):
                size_mb = os.path.getsize(db_file) / 1024 / 1024
                self.add_line(db_file + " (" + str(round(size_mb, 2)) + " MB)")
        self.add_line("```")
        
        # 6. Scripts
        self.add_section("6. SCRIPTS")
        
        scripts = glob.glob("scripts/*.py") + glob.glob("scripts/*.sh")
        self.add_line("")
        self.add_line("### Available Scripts:")
        self.add_line("```")
        for script in sorted(scripts):
            if os.path.exists(script):
                size = os.path.getsize(script)
                self.add_line(script + " (" + str(size) + " bytes)")
        self.add_line("```")
        
        # 7. Errors
        self.add_section("7. BUILD ERRORS")
        
        if os.path.exists("compile_errors.log"):
            self.add_line("")
            self.add_line("### Recent Compilation Errors:")
            self.add_line("```")
            try:
                with open("compile_errors.log", 'r') as f:
                    lines = f.readlines()
                    self.add_line(''.join(lines[-50:]))
            except:
                self.add_line("Could not read compile_errors.log")
            self.add_line("```")
        
        # 8. Statistics
        self.add_section("8. PROJECT STATISTICS")
        
        self.add_line("")
        self.add_line("### Code Metrics:")
        self.add_line("- Total Java Files: " + str(self.stats.get('java_files', 0)))
        self.add_line("- Total Lines of Code: " + str(self.stats.get('total_lines', 0)))
        
        self.add_line("")
        self.add_line("### File Count by Type:")
        self.add_line("```")
        self.add_line(".java files: " + str(len(glob.glob('**/*.java', recursive=True))))
        self.add_line(".xml files: " + str(len(glob.glob('**/*.xml', recursive=True))))
        self.add_line(".properties files: " + str(len(glob.glob('**/*.properties', recursive=True))))
        self.add_line("```")
        
        # 9. Project Specs
        self.add_section("9. PROJECT SPECIFICATIONS")
        
        specs = [
            "",
            "### Problem Statement:",
            "- Orphanages face resource management and transparency challenges",
            "- Need centralized system for tracking donations and volunteers",
            "",
            "### Solution:",
            "- Java Swing desktop application",
            "- MS Access database backend",
            "- Multi-role user system",
            "",
            "### Target Users:",
            "1. Orphanage Administrators",
            "2. Donors",
            "3. Volunteers",
            "4. System Administrators",
            "",
            "### Core Features:",
            "- User authentication and role management",
            "- Resource request tracking",
            "- Donation management",
            "- Volunteer opportunity coordination",
            "- Reporting and analytics",
            "",
            "### Technical Stack:",
            "- Java 17+",
            "- Maven 3.8+",
            "- MS Access (via UCanAccess)",
            "- Java Swing GUI",
            "",
            "### Database Tables:",
            "- TblUsers",
            "- TblOrphanages",
            "- TblResourceRequests",
            "- TblDonations",
            "- TblVolunteerOpportunities",
            "- TblInventory"
        ]
        
        for line in specs:
            self.add_line(line)
        
        # 10. Documentation
        self.add_section("10. DOCUMENTATION")
        self.add_file("README.md", "markdown")
        
        # Quick Start
        self.add_line("")
        self.add_line("### Quick Start:")
        self.add_line("```bash")
        self.add_line("# Build")
        self.add_line("make clean")
        self.add_line("make compile")
        self.add_line("")
        self.add_line("# Run")
        self.add_line("make run")
        self.add_line("")
        self.add_line("# Test")
        self.add_line("make test")
        self.add_line("```")
        
        # Footer
        self.add_line("")
        self.add_line("=" * 80)
        self.add_line("Configuration generated at: " + str(datetime.datetime.now()))
        self.add_line("=" * 80)
        
        # Write to file
        with open(self.output_file, 'w', encoding='utf-8') as f:
            f.write('\n'.join(self.content))
        
        # Success message
        file_size_kb = os.path.getsize(self.output_file) / 1024
        print("✅ Configuration generated successfully!")
        print("📄 File: " + self.output_file)
        print("📊 Size: " + str(round(file_size_kb, 2)) + " KB")
        print("📝 Lines: " + str(len(self.content)))
        print("📍 Location: " + os.path.abspath(self.output_file))

# Run the generator
if __name__ == "__main__":
    generator = ConfigGenerator()
    generator.generate()
