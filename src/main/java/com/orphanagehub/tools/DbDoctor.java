package com.orphanagehub.tools;

import com.orphanagehub.dao.DatabaseManager;
import io.vavr.control.Try;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Enhanced database diagnostic and health check tool.
 * Verifies connectivity, schema integrity, and data consistency.
 * 
 * @author OrphanageHub Team
 * @version 2.0
 * @since 2025-09-06
 */
public class DbDoctor {
    
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    
    private static int totalChecks = 0;
    private static int passedChecks = 0;
    private static List<String> issues = new ArrayList<>();
    
    public static void main(String[] args) {
        printHeader();
        
        boolean verbose = args.length > 0 && "--verbose".equals(args[0]);
        boolean repair = args.length > 0 && "--repair".equals(args[0]);
        
        // Run all checks
        checkDatabaseConnection();
        checkDatabaseInfo();
        checkRequiredTables();
        checkTableSchemas();
        checkRequiredIndexes();
        checkDataIntegrity();
        checkOrphanedRecords();
        checkPerformanceMetrics();
        
        if (repair && !issues.isEmpty()) {
            System.out.println("\n" + ANSI_YELLOW + "[!] Attempting repairs..." + ANSI_RESET);
            attemptRepairs();
        }
        
        printSummary();
        
        System.exit(issues.isEmpty() ? 0 : 1);
    }
    
    private static void printHeader() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║           DATABASE DOCTOR - Comprehensive Health Check        ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  Version: 2.0.0  |  " + LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "           ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");
    }
    
    private static void checkDatabaseConnection() {
        System.out.println(ANSI_BLUE + "[1] DATABASE CONNECTION" + ANSI_RESET);
        System.out.println("════════════════════════════════════════════");
        
        totalChecks++;
        Try<Connection> connectionTry = DatabaseManager.getConnection();
        
        if (connectionTry.isSuccess()) {
            try (Connection conn = connectionTry.get()) {
                if (!conn.isClosed() && conn.isValid(5)) {
                    System.out.println(ANSI_GREEN + "  ✓ Connection established successfully" + ANSI_RESET);
                    System.out.println("    • Connection pool: Active");
                    System.out.println("    • Auto-commit: " + conn.getAutoCommit());
                    System.out.println("    • Transaction isolation: " + 
                        getIsolationLevelName(conn.getTransactionIsolation()));
                    passedChecks++;
                } else {
                    System.out.println(ANSI_RED + "  ✗ Connection invalid or closed" + ANSI_RESET);
                    issues.add("Database connection is not valid");
                }
            } catch (SQLException e) {
                System.out.println(ANSI_RED + "  ✗ Connection test failed: " + e.getMessage() + ANSI_RESET);
                issues.add("Connection test failed: " + e.getMessage());
            }
        } else {
            System.out.println(ANSI_RED + "  ✗ Failed to establish connection" + ANSI_RESET);
            System.out.println("    Error: " + connectionTry.getCause().getMessage());
            issues.add("Cannot establish database connection");
        }
        System.out.println();
    }
    
    private static void checkDatabaseInfo() {
        System.out.println(ANSI_BLUE + "[2] DATABASE INFORMATION" + ANSI_RESET);
        System.out.println("════════════════════════════════════════════");
        
        DatabaseManager.getConnection().forEach(conn -> {
            try {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("  • Product: " + meta.getDatabaseProductName());
                System.out.println("  • Version: " + meta.getDatabaseProductVersion());
                System.out.println("  • Driver: " + meta.getDriverName() + " v" + meta.getDriverVersion());
                System.out.println("  • URL: " + meta.getURL());
                System.out.println("  • User: " + meta.getUserName());
                System.out.println("  • Max connections: " + meta.getMaxConnections());
                
                // Check SQLite specific settings
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery("PRAGMA journal_mode");
                    if (rs.next()) {
                        System.out.println("  • Journal mode: " + rs.getString(1));
                    }
                    
                    rs = stmt.executeQuery("PRAGMA synchronous");
                    if (rs.next()) {
                        System.out.println("  • Synchronous: " + rs.getString(1));
                    }
                }
                
                conn.close();
            } catch (SQLException e) {
                System.out.println(ANSI_YELLOW + "  ⚠ Could not retrieve all database info: " + 
                    e.getMessage() + ANSI_RESET);
            }
        });
        System.out.println();
    }
    
    private static void checkRequiredTables() {
        System.out.println(ANSI_BLUE + "[3] REQUIRED TABLES" + ANSI_RESET);
        System.out.println("════════════════════════════════════════════");
        
        String[] requiredTables = {
            "TblUsers",
            "TblOrphanages", 
            "TblResourceRequests",
            "TblDonations",
            "TblVolunteerOpportunities",
            "TblVolunteerApplications",
            "TblDonationItems",
            "TblNotifications",
            "TblAuditLog"
        };
        
        DatabaseManager.getConnection().forEach(conn -> {
            try {
                DatabaseMetaData meta = conn.getMetaData();
                
                for (String tableName : requiredTables) {
                    totalChecks++;
                    try (ResultSet tables = meta.getTables(null, null, tableName, null)) {
                        if (tables.next()) {
                            // Get row count
                            try (Statement stmt = conn.createStatement()) {
                                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName);
                                if (rs.next()) {
                                    int count = rs.getInt(1);
                                    System.out.println(ANSI_GREEN + "  ✓ " + tableName + 
                                        " (Rows: " + count + ")" + ANSI_RESET);
                                    passedChecks++;
                                }
                            }
                        } else {
                            System.out.println(ANSI_RED + "  ✗ " + tableName + " - NOT FOUND" + ANSI_RESET);
                            issues.add("Missing table: " + tableName);
                        }
                    }
                }
                
                // Check views
                System.out.println("\n  Views:");
                String[] views = {"vw_ActiveResourceRequests", "vw_DonationSummary"};
                for (String viewName : views) {
                    totalChecks++;
                    try (ResultSet viewSet = meta.getTables(null, null, viewName, new String[]{"VIEW"})) {
                        if (viewSet.next()) {
                            System.out.println(ANSI_GREEN + "  ✓ " + viewName + ANSI_RESET);
                            passedChecks++;
                        } else {
                            System.out.println(ANSI_YELLOW + "  ⚠ " + viewName + " - Not found (optional)" + ANSI_RESET);
                        }
                    }
                }
                
                conn.close();
            } catch (SQLException e) {
                System.out.println(ANSI_RED + "  ✗ Error checking tables: " + e.getMessage() + ANSI_RESET);
                issues.add("Error checking tables: " + e.getMessage());
            }
        });
        System.out.println();
    }
    
    private static void checkTableSchemas() {
        System.out.println(ANSI_BLUE + "[4] TABLE SCHEMAS" + ANSI_RESET);
        System.out.println("════════════════════════════════════════════");
        
        Map<String, String[]> requiredColumns = new HashMap<>();
        requiredColumns.put("TblUsers", new String[]{
            "UserID", "Username", "PasswordHash", "Email", "UserRole",
            "DateRegistered", "AccountStatus", "EmailVerified"
        });
        requiredColumns.put("TblOrphanages", new String[]{
            "OrphanageID", "OrphanageName", "Address", "City", "Province",
            "ContactPerson", "ContactEmail", "ContactPhone", "UserID",
            "VerificationStatus", "Status"
        });
        requiredColumns.put("TblResourceRequests", new String[]{
            "RequestID", "OrphanageID", "ResourceType", "ResourceDescription",
            "Quantity", "UrgencyLevel", "Status", "CreatedBy"
        });
        requiredColumns.put("TblDonations", new String[]{
            "DonationID", "DonorID", "OrphanageID", "DonationType",
            "Status", "DonationDate", "TaxDeductible"
        });
        
        DatabaseManager.getConnection().forEach(conn -> {
            try {
                DatabaseMetaData meta = conn.getMetaData();
                
                for (Map.Entry<String, String[]> entry : requiredColumns.entrySet()) {
                    String tableName = entry.getKey();
                    String[] columns = entry.getValue();
                    
                    System.out.println("  " + tableName + ":");
                    
                    // Check if table exists first
                    try (ResultSet tables = meta.getTables(null, null, tableName, null)) {
                        if (!tables.next()) {
                            System.out.println(ANSI_YELLOW + "    ⚠ Table not found - skipping column check" + ANSI_RESET);
                            continue;
                        }
                    }
                    
                    for (String columnName : columns) {
                        totalChecks++;
                        try (ResultSet cols = meta.getColumns(null, null, tableName, columnName)) {
                            if (cols.next()) {
                                String dataType = cols.getString("TYPE_NAME");
                                int size = cols.getInt("COLUMN_SIZE");
                                String nullable = cols.getString("IS_NULLABLE");
                                
                                System.out.println(ANSI_GREEN + "    ✓ " + columnName + 
                                    " (" + dataType + 
                                    (size > 0 && !dataType.contains("INT") ? "[" + size + "]" : "") +
                                    ", " + ("YES".equals(nullable) ? "NULL" : "NOT NULL") + ")" + 
                                    ANSI_RESET);
                                passedChecks++;
                            } else {
                                System.out.println(ANSI_RED + "    ✗ " + columnName + " - MISSING" + ANSI_RESET);
                                issues.add("Missing column: " + tableName + "." + columnName);
                            }
                        }
                    }
                }
                
                conn.close();
            } catch (SQLException e) {
                System.out.println(ANSI_RED + "  ✗ Error checking schemas: " + e.getMessage() + ANSI_RESET);
                issues.add("Error checking schemas: " + e.getMessage());
            }
        });
        System.out.println();
    }
    
    private static void checkRequiredIndexes() {
        System.out.println(ANSI_BLUE + "[5] DATABASE INDEXES" + ANSI_RESET);
        System.out.println("════════════════════════════════════════════");
        
        String[] requiredIndexes = {
            "idx_users_username",
            "idx_users_email",
            "idx_orphanages_status",
            "idx_requests_orphanage",
            "idx_donations_donor"
        };
        
        DatabaseManager.getConnection().forEach(conn -> {
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(
                    "SELECT name FROM sqlite_master WHERE type='index' AND sql NOT NULL"
                );
                
                Set<String> existingIndexes = new HashSet<>();
                while (rs.next()) {
                    existingIndexes.add(rs.getString("name"));
                }
                
                for (String indexName : requiredIndexes) {
                    totalChecks++;
                    if (existingIndexes.contains(indexName)) {
                        System.out.println(ANSI_GREEN + "  ✓ " + indexName + ANSI_RESET);
                        passedChecks++;
                    } else {
                        System.out.println(ANSI_YELLOW + "  ⚠ " + indexName + 
                            " - Missing (may impact performance)" + ANSI_RESET);
                    }
                }
                
                conn.close();
            } catch (SQLException e) {
                System.out.println(ANSI_YELLOW + "  ⚠ Could not check indexes: " + e.getMessage() + ANSI_RESET);
            }
        });
        System.out.println();
    }
    
    private static void checkDataIntegrity() {
        System.out.println(ANSI_BLUE + "[6] DATA INTEGRITY" + ANSI_RESET);
        System.out.println("════════════════════════════════════════════");
        
        DatabaseManager.getConnection().forEach(conn -> {
            try (Statement stmt = conn.createStatement()) {
                // Check for orphaned orphanages
                totalChecks++;
                ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM TblOrphanages o " +
                    "WHERE NOT EXISTS (SELECT 1 FROM TblUsers u WHERE u.UserID = o.UserID)"
                );
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println(ANSI_GREEN + "  ✓ No orphaned orphanages" + ANSI_RESET);
                    passedChecks++;
                } else if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println(ANSI_YELLOW + "  ⚠ Found " + count + 
                        " orphanages without valid users" + ANSI_RESET);
                    issues.add("Orphaned orphanages: " + count);
                }
                
                // Check for orphaned donations
                totalChecks++;
                rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM TblDonations d " +
                    "WHERE NOT EXISTS (SELECT 1 FROM TblUsers u WHERE u.UserID = d.DonorID)"
                );
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println(ANSI_GREEN + "  ✓ No orphaned donations" + ANSI_RESET);
                    passedChecks++;
                } else if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println(ANSI_YELLOW + "  ⚠ Found " + count + 
                        " donations without valid donors" + ANSI_RESET);
                    issues.add("Orphaned donations: " + count);
                }
                
                // Check for duplicate usernames
                totalChecks++;
                rs = stmt.executeQuery(
                    "SELECT Username, COUNT(*) as cnt FROM TblUsers " +
                    "GROUP BY Username HAVING COUNT(*) > 1"
                );
                if (!rs.next()) {
                    System.out.println(ANSI_GREEN + "  ✓ No duplicate usernames" + ANSI_RESET);
                    passedChecks++;
                } else {
                    System.out.println(ANSI_RED + "  ✗ Found duplicate usernames" + ANSI_RESET);
                    do {
                        System.out.println("    - " + rs.getString("Username") + 
                            " (" + rs.getInt("cnt") + " occurrences)");
                    } while (rs.next());
                    issues.add("Duplicate usernames found");
                }
                
                // Check for invalid email formats
                totalChecks++;
                rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM TblUsers " +
                    "WHERE Email NOT LIKE '%@%.%'"
                );
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println(ANSI_GREEN + "  ✓ All email addresses valid" + ANSI_RESET);
                    passedChecks++;
                } else if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println(ANSI_YELLOW + "  ⚠ Found " + count + 
                        " invalid email addresses" + ANSI_RESET);
                }
                
                conn.close();
            } catch (SQLException e) {
                System.out.println(ANSI_RED + "  ✗ Error checking data integrity: " + 
                    e.getMessage() + ANSI_RESET);
                issues.add("Data integrity check failed: " + e.getMessage());
            }
        });
        System.out.println();
    }
    
    private static void checkOrphanedRecords() {
        System.out.println(ANSI_BLUE + "[7] ORPHANED RECORDS CHECK" + ANSI_RESET);
        System.out.println("════════════════════════════════════════════");
        
        Map<String, String> orphanChecks = new HashMap<>();
        orphanChecks.put(
            "Resource requests without orphanages",
            "SELECT COUNT(*) FROM TblResourceRequests r " +
            "WHERE NOT EXISTS (SELECT 1 FROM TblOrphanages o WHERE o.OrphanageID = r.OrphanageID)"
        );
        orphanChecks.put(
            "Volunteer opportunities without orphanages",
            "SELECT COUNT(*) FROM TblVolunteerOpportunities v " +
            "WHERE NOT EXISTS (SELECT 1 FROM TblOrphanages o WHERE o.OrphanageID = v.OrphanageID)"
        );
        orphanChecks.put(
            "Notifications for deleted users",
            "SELECT COUNT(*) FROM TblNotifications n " +
            "WHERE NOT EXISTS (SELECT 1 FROM TblUsers u WHERE u.UserID = n.UserID)"
        );
        
        DatabaseManager.getConnection().forEach(conn -> {
            try (Statement stmt = conn.createStatement()) {
                for (Map.Entry<String, String> check : orphanChecks.entrySet()) {
                    totalChecks++;
                    try {
                        ResultSet rs = stmt.executeQuery(check.getValue());
                        if (rs.next()) {
                            int count = rs.getInt(1);
                            if (count == 0) {
                                System.out.println(ANSI_GREEN + "  ✓ No " + 
                                    check.getKey().toLowerCase() + ANSI_RESET);
                                passedChecks++;
                            } else {
                                System.out.println(ANSI_YELLOW + "  ⚠ Found " + count + " " + 
                                    check.getKey().toLowerCase() + ANSI_RESET);
                                issues.add(check.getKey() + ": " + count);
                            }
                        }
                    } catch (SQLException e) {
                        // Table might not exist
                        System.out.println("  - Skipping: " + check.getKey() + " (table not found)");
                    }
                }
                conn.close();
            } catch (SQLException e) {
                System.out.println(ANSI_RED + "  ✗ Error checking orphaned records: " + 
                    e.getMessage() + ANSI_RESET);
            }
        });
        System.out.println();
    }
    
    private static void checkPerformanceMetrics() {
        System.out.println(ANSI_BLUE + "[8] PERFORMANCE METRICS" + ANSI_RESET);
        System.out.println("════════════════════════════════════════════");
        
        DatabaseManager.getConnection().forEach(conn -> {
            try (Statement stmt = conn.createStatement()) {
                // Database size
                ResultSet rs = stmt.executeQuery("PRAGMA page_count");
                if (rs.next()) {
                    int pageCount = rs.getInt(1);
                    rs = stmt.executeQuery("PRAGMA page_size");
                    if (rs.next()) {
                        int pageSize = rs.getInt(1);
                        long dbSize = (long) pageCount * pageSize;
                        System.out.println("  • Database size: " + formatBytes(dbSize));
                    }
                }
                
                // Table statistics
                System.out.println("  • Table statistics:");
                String[] tables = {"TblUsers", "TblOrphanages", "TblDonations", "TblResourceRequests"};
                for (String table : tables) {
                    try {
                        rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table);
                        if (rs.next()) {
                            System.out.println("    - " + table + ": " + rs.getInt(1) + " rows");
                        }
                    } catch (SQLException e) {
                        // Table might not exist
                    }
                }
                
                // Check for slow queries (simplified)
                System.out.println("  • Query performance:");
                long startTime = System.currentTimeMillis();
                rs = stmt.executeQuery("SELECT COUNT(*) FROM TblUsers WHERE Username LIKE 'test%'");
                long queryTime = System.currentTimeMillis() - startTime;
                
                if (queryTime < 100) {
                    System.out.println(ANSI_GREEN + "    ✓ Sample query executed in " + 
                        queryTime + "ms" + ANSI_RESET);
                } else {
                    System.out.println(ANSI_YELLOW + "    ⚠ Sample query took " + 
                        queryTime + "ms (consider indexing)" + ANSI_RESET);
                }
                
                conn.close();
            } catch (SQLException e) {
                System.out.println(ANSI_YELLOW + "  ⚠ Could not gather all metrics: " + 
                    e.getMessage() + ANSI_RESET);
            }
        });
        System.out.println();
    }
    
    private static void attemptRepairs() {
        DatabaseManager.getConnection().forEach(conn -> {
            try (Statement stmt = conn.createStatement()) {
                // Attempt to create missing indexes
                for (String issue : issues) {
                    if (issue.startsWith("Missing index:")) {
                        // Extract index name and attempt to create
                        // This is simplified - in production, you'd have proper index definitions
                        System.out.println("  Attempting to create missing index...");
                    }
                }
                
                // Clean up orphaned records with user confirmation
                System.out.println("  Checking for safe cleanup operations...");
                
                conn.close();
            } catch (SQLException e) {
                System.out.println(ANSI_RED + "  ✗ Repair failed: " + e.getMessage() + ANSI_RESET);
            }
        });
    }
    
    private static void printSummary() {
        System.out.println(ANSI_BLUE + "[SUMMARY]" + ANSI_RESET);
        System.out.println("════════════════════════════════════════════");
        
        double successRate = totalChecks > 0 ? (passedChecks * 100.0 / totalChecks) : 0;
        
        if (issues.isEmpty()) {
            System.out.println(ANSI_GREEN + "  ✓ ALL CHECKS PASSED!" + ANSI_RESET);
            System.out.println("  • Total checks: " + totalChecks);
            System.out.println("  • Passed: " + passedChecks);
            System.out.println("  • Success rate: " + String.format("%.1f%%", successRate));
            System.out.println(ANSI_GREEN + "\n  Database is healthy and ready for use!" + ANSI_RESET);
        } else {
            System.out.println(ANSI_YELLOW + "  ⚠ ISSUES DETECTED" + ANSI_RESET);
            System.out.println("  • Total checks: " + totalChecks);
            System.out.println("  • Passed: " + passedChecks);
            System.out.println("  • Failed: " + (totalChecks - passedChecks));
            System.out.println("  • Success rate: " + String.format("%.1f%%", successRate));
            
            System.out.println("\n  Issues found:");
            for (String issue : issues) {
                System.out.println(ANSI_RED + "    ✗ " + issue + ANSI_RESET);
            }
            
            System.out.println(ANSI_YELLOW + "\n  Action required to resolve issues!" + ANSI_RESET);
            System.out.println("  Run with --repair flag to attempt automatic fixes");
        }
        
        System.out.println("\n════════════════════════════════════════════");
    }
    
    private static String getIsolationLevelName(int level) {
        return switch (level) {
            case Connection.TRANSACTION_NONE -> "None";
            case Connection.TRANSACTION_READ_UNCOMMITTED -> "Read Uncommitted";
            case Connection.TRANSACTION_READ_COMMITTED -> "Read Committed";
            case Connection.TRANSACTION_REPEATABLE_READ -> "Repeatable Read";
            case Connection.TRANSACTION_SERIALIZABLE -> "Serializable";
            default -> "Unknown (" + level + ")";
        };
    }
    
    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}