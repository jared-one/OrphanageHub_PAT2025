package com.orphanagehub.tools;

import com.orphanagehub.dao.DatabaseManager;
import io.vavr.control.Try;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

/**
 * Database diagnostic tool to verify database connectivity and schema.
 */
public class DbDoctor {
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║        Database Doctor - Health Check Tool        ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");
        
        System.out.println("[1] Testing database connection...");
        
        Try<Connection> connectionTry = DatabaseManager.getConnection();
        
        if (connectionTry.isSuccess()) {
            System.out.println("✓ Database connection successful!");
            
            try (Connection conn = connectionTry.get()) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("\n[2] Database Information:");
                System.out.println("   Product: " + meta.getDatabaseProductName());
                System.out.println("   Version: " + meta.getDatabaseProductVersion());
                System.out.println("   Driver:  " + meta.getDriverName() + " v" + meta.getDriverVersion());
                
                System.out.println("\n[3] Checking required tables:");
                checkTable(meta, "TblUsers");
                checkTable(meta, "TblOrphanages");
                checkTable(meta, "TblResourceRequests");
                checkTable(meta, "TblDonations");
                checkTable(meta, "TblVolunteerOpportunities");
                
                System.out.println("\n[4] Database schema verification:");
                verifyUserTableColumns(conn);
                
                System.out.println("\n✓ All checks completed successfully!");
                
            } catch (SQLException e) {
                System.err.println("✗ Error during health check: " + e.getMessage());
                System.exit(1);
            }
        } else {
            System.err.println("✗ Database connection failed!");
            System.err.println("   Error: " + connectionTry.getCause().getMessage());
            System.err.println("\nTroubleshooting tips:");
            System.err.println("  1. Check if database file exists: db/OrphanageHub.accdb");
            System.err.println("  2. Ensure UCanAccess driver is in classpath");
            System.err.println("  3. Verify file permissions on database file");
            System.exit(1);
        }
    }
    
    private static void checkTable(DatabaseMetaData meta, String tableName) {
        try (ResultSet tables = meta.getTables(null, null, tableName, null)) {
            if (tables.next()) {
                System.out.println("   ✓ " + tableName + " exists");
            } else {
                System.out.println("   ✗ " + tableName + " NOT FOUND");
            }
        } catch (SQLException e) {
            System.err.println("   ✗ Error checking " + tableName + ": " + e.getMessage());
        }
    }
    
    private static void verifyUserTableColumns(Connection conn) {
        String[] requiredColumns = {
            "UserID", "Username", "PasswordHash", "Email", "UserRole", 
            "DateRegistered", "FullName", "AccountStatus"
        };
        
        System.out.println("   Checking TblUsers columns:");
        for (String column : requiredColumns) {
            if (columnExists(conn, "TblUsers", column)) {
                System.out.println("     ✓ " + column);
            } else {
                System.out.println("     ✗ " + column + " missing");
            }
        }
    }
    
    private static boolean columnExists(Connection conn, String tableName, String columnName) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet columns = meta.getColumns(null, null, tableName, columnName)) {
                return columns.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }
}
