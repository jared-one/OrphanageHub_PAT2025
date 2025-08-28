import os
import shutil

# Define the base path (assuming run from project root)
base_path = 'src/main/java/com/orphanagehub'

# Helper to fix a file with replacements or insertions
def fix_file(file_path, replacements=None, insertions=None):
    full_path = os.path.join(base_path, file_path)
    if not os.path.exists(full_path):
        print(f"File not found: {full_path}")
        return
    # Backup original
    shutil.copy(full_path, full_path + '.bak')
    with open(full_path, 'r') as f:
        lines = f.readlines()
    # Apply replacements (list of (old, new))
    if replacements:
        for i, line in enumerate(lines):
            for old, new in replacements:
                lines[i] = lines[i].replace(old, new)
    # Apply insertions (list of (line_num - 1, new_line)) - but we use dynamic idx
    if insertions:
        for insert_idx, new_line in sorted(insertions, reverse=True):  # Reverse to avoid offset issues
            lines.insert(insert_idx, new_line)
    # Write back
    with open(full_path, 'w') as f:
        f.writelines(lines)
    print(f"Fixed: {full_path}")

# 1. OrphanageDashboardPanel.java
fix_file('gui/OrphanageDashboardPanel.java', replacements=[
    ('mainApp.logout();', 'logoutAction.run();'),
    ('Color.LIGHTGRAY', 'Color.LIGHT_GRAY')
])

# 2. RegistrationPanel.java
fix_file('gui/RegistrationPanel.java', replacements=[
    ('Color.LIGHTGRAY', 'Color.LIGHT_GRAY')
])

# 3. DonorDashboardPanel.java
fix_file('gui/DonorDashboardPanel.java', replacements=[
    ('static class ButtonEditor', 'class ButtonEditor')  # Remove static
])

# 4. VolunteerDashboardPanel.java
fix_file('gui/VolunteerDashboardPanel.java', replacements=[
    ('Color.LIGHTGRAY', 'Color.LIGHT_GRAY')
])

# 5. DbShell.java - Replace the entire main method with fixed version (more precise)
db_path = os.path.join(base_path, 'tools/DbShell.java')
if os.path.exists(db_path):
    shutil.copy(db_path, db_path + '.bak')
    with open(db_path, 'r') as f:
        lines = f.readlines()
    # Find start of main method: public static void main(String[] args) {
    main_start = next(i for i, line in enumerate(lines) if 'public static void main(String[] args) {' in line.strip())
    # Find end of main method: the closing } of the method (next } after main_start, assuming no nested methods)
    brace_count = 0
    main_end = main_start
    for i in range(main_start, len(lines)):
        line = lines[i].strip()
        brace_count += line.count('{')
        brace_count -= line.count('}')
        if brace_count == 0 and i > main_start:
            main_end = i + 1  # Include the closing }
            break
    # Fixed main method block (full, with correct indentation and catch)
    fixed_main = """    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: make db-sql q=\\\"YOUR_QUERY\\\"");
            System.exit(1);
        }
        String query = args[0];
        System.out.println("Executing: " + query);
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            boolean hasResultSet = stmt.execute(query);
            if (hasResultSet) {
                try (ResultSet rs = stmt.getResultSet()) {
                    ResultSetMetaData meta = rs.getMetaData();
                    int colCount = meta.getColumnCount();
                    for (int i = 1; i <= colCount; i++) {
                        System.out.printf("%-25s", meta.getColumnName(i));
                    }
                    System.out.println("\\n" + "-".repeat(colCount * 25));
                    while (rs.next()) {
                        for (int i = 1; i <= colCount; i++) {
                            System.out.printf("%-25s", rs.getString(i));
                        }
                        System.out.println();
                    }
                }
            } else {
                System.out.println("Query OK, " + stmt.getUpdateCount() + " rows affected.");
            }
        } catch (SQLException e) {
            System.err.println("Query failed: " + e.getMessage());
            System.exit(1);
        }
    }
""".splitlines(True)  # Keep newlines and indentation
    # Replace the entire main block
    lines[main_start:main_end] = fixed_main
    with open(db_path, 'w') as f:
        f.writelines(lines)
    print(f"Fixed: {db_path}")

# 6. User.java - Insert no-arg constructor after last field
user_path = os.path.join(base_path, 'model/User.java')
if os.path.exists(user_path):
    shutil.copy(user_path, user_path + '.bak')
    with open(user_path, 'r') as f:
        lines = f.readlines()
    # Find insertion point: after 'private Timestamp dateRegistered;'
    insert_idx = next(i for i, line in enumerate(lines) if 'private Timestamp dateRegistered;' in line.strip()) + 2
    lines.insert(insert_idx, '    public User() {}\n')
    lines.insert(insert_idx + 1, '\n')  # Extra newline
    with open(user_path, 'w') as f:
        f.writelines(lines)
    print(f"Fixed: {user_path}")

print("All fixes applied! Backups created with .bak extension. Run 'make build' to test.")
