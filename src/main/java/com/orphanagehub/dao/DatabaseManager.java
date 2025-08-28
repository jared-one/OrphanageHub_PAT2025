package com.orphanagehub.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String DB_PATH = "db/OrphanageHub.accdb";
    private static final String CONNECTION_STRING = "jdbc:ucanaccess://" + DB_PATH + ";immediatelyReleaseResources=true";

    static {
        try {
            initializeDatabase();
        } catch (SQLException e) {
            logger.error("Failed to initialize database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(CONNECTION_STRING);
    }

    private static void initializeDatabase() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            // Check if FullName column exists in TblUsers, add if not
            if (!columnExists(conn, "TblUsers", "FullName")) {
                stmt.execute("ALTER TABLE TblUsers ADD COLUMN FullName TEXT(100)");
                logger.info("Added FullName column to TblUsers");
            }

            // Check if AccountStatus column exists in TblUsers, add if not
            if (!columnExists(conn, "TblUsers", "AccountStatus")) {
                stmt.execute("ALTER TABLE TblUsers ADD COLUMN AccountStatus TEXT(20) DEFAULT 'Active'");
                logger.info("Added AccountStatus column to TblUsers");
            }
        }
    }

    public static void verifyTables() throws SQLException {
        try (Connection conn = getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            
            // Verify TblUsers
            try (ResultSet tables = meta.getTables(null, null, "TblUsers", null)) {
                if (!tables.next()) {
                    logger.warn("TblUsers table does not exist");
                    return;
                }
            }
            
            // Verify required columns in TblUsers
            String[] requiredColumns = {"UserID", "Username", "PasswordHash", "Email", "UserRole", "DateRegistered", "FullName", "AccountStatus"};
            for (String column : requiredColumns) {
                if (!columnExists(conn, "TblUsers", column)) {
                    logger.warn("Missing column in TblUsers: " + column);
                }
            }
            
            logger.info("Tables verified successfully");
        }
    }

    private static boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet columns = meta.getColumns(null, null, tableName, columnName)) {
            return columns.next();
        }
    }
}