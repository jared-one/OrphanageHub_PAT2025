// src/main/java/com/orphanagehub/dao/DatabaseManager.java
/* Copyright (C) 2025 Jared Wisdom - All Rights Reserved */
package com.orphanagehub.dao;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static final Properties p = new Properties();
    private static boolean initialized = false;
    static {
        try (InputStream in = DatabaseManager.class.getResourceAsStream("/app.properties")) {
            if (in != null) {
                p.load(in);
                logger.info("Properties loaded successfully");
            } else {
                logger.warn("app.properties not found, using defaults");
            }
        } catch (IOException e) {
            logger.error("Error loading properties: " + e.getMessage(), e);
        }
        File dbDir = new File("db");
        if (!dbDir.exists()) {
            if (dbDir.mkdirs()) {
                logger.info("Created database directory: db/");
            } else {
                logger.error("Failed to create database directory: db/");
            }
        }
        initializeDatabase();
    }
    private DatabaseManager() {}
    private static boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, tableName.toUpperCase(), new String[]{"TABLE"})) {
            return rs.next();
        }
    }
    private static void addColumnIfNotExists(Connection conn, String tableName, String columnName, String columnType) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(null, null, tableName.toUpperCase(), columnName.toUpperCase())) {
            if (!rs.next()) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnType);
                    logger.info("Added column {} to table {}", columnName, tableName);
                }
            } else {
                logger.debug("Column {} already exists in table {}", columnName, tableName);
            }
        }
    }
    private static void initializeDatabase() {
        String url = p.getProperty("db.url", "jdbc:ucanaccess://db/OrphanageHub.accdb;newDatabaseVersion=V2010");
       
        try (Connection conn = DriverManager.getConnection(url)) {
            logger.info("Database connection established successfully");
           
            // Create Users table if it doesn't exist
            if (!tableExists(conn, "Users")) {
                try (Statement stmt = conn.createStatement()) {
                    String createUsersTable =
                        "CREATE TABLE Users (" +
                        "UserID VARCHAR(36) PRIMARY KEY, " +
                        "Username VARCHAR(50) UNIQUE NOT NULL, " +
                        "PasswordHash VARCHAR(255) NOT NULL, " +
                        "Email VARCHAR(100) UNIQUE NOT NULL, " +
                        "FullName VARCHAR(100), " +
                        "UserRole VARCHAR(20) NOT NULL, " +
                        "DateRegistered DATETIME, " +
                        "AccountStatus VARCHAR(20) DEFAULT 'ACTIVE'" +
                        ")";
                   
                    stmt.execute(createUsersTable);
                    logger.info("Users table created successfully");
                }
            } else {
                logger.debug("Users table already exists");
            }
            // Add OrphanageID column to Users if not exists
            addColumnIfNotExists(conn, "Users", "OrphanageID", "VARCHAR(36)");
            // Create Orphanages table if it doesn't exist
            if (!tableExists(conn, "Orphanages")) {
                try (Statement stmt = conn.createStatement()) {
                    String createOrphanagesTable =
                        "CREATE TABLE Orphanages (" +
                        "OrphanageID VARCHAR(36) PRIMARY KEY, " +
                        "Name VARCHAR(100) NOT NULL, " +
                        "Address VARCHAR(255), " +
                        "ContactEmail VARCHAR(100), " +
                        "ContactPhone VARCHAR(20), " +
                        "Description VARCHAR(500), " +
                        "Capacity INTEGER, " +
                        "CurrentOccupancy INTEGER DEFAULT 0, " +
                        "DateEstablished DATETIME, " +
                        "Status VARCHAR(20) DEFAULT 'ACTIVE'" +
                        ")";
                   
                    stmt.execute(createOrphanagesTable);
                    logger.info("Orphanages table created successfully");
                }
            } else {
                logger.debug("Orphanages table already exists");
            }
            // Create Donations table if it doesn't exist
            if (!tableExists(conn, "Donations")) {
                try (Statement stmt = conn.createStatement()) {
                    String createDonationsTable =
                        "CREATE TABLE Donations (" +
                        "DonationID VARCHAR(36) PRIMARY KEY, " +
                        "DonorID VARCHAR(36), " +
                        "OrphanageID VARCHAR(36), " +
                        "Amount DECIMAL(10,2), " +
                        "DonationType VARCHAR(50), " +
                        "Description VARCHAR(500), " +
                        "DonationDate DATETIME, " +
                        "Status VARCHAR(20) DEFAULT 'PENDING'" +
                        ")";
                   
                    stmt.execute(createDonationsTable);
                    logger.info("Donations table created successfully");
                }
            } else {
                logger.debug("Donations table already exists");
            }
            // Create VolunteerActivities table if it doesn't exist
            if (!tableExists(conn, "VolunteerActivities")) {
                try (Statement stmt = conn.createStatement()) {
                    String createVolunteerTable =
                        "CREATE TABLE VolunteerActivities (" +
                        "ActivityID VARCHAR(36) PRIMARY KEY, " +
                        "VolunteerID VARCHAR(36), " +
                        "OrphanageID VARCHAR(36), " +
                        "ActivityType VARCHAR(50), " +
                        "Description VARCHAR(500), " +
                        "ScheduledDate DATETIME, " +
                        "Duration INTEGER, " +
                        "Status VARCHAR(20) DEFAULT 'SCHEDULED'" +
                        ")";
                   
                    stmt.execute(createVolunteerTable);
                    logger.info("VolunteerActivities table created successfully");
                }
            } else {
                logger.debug("VolunteerActivities table already exists");
            }
            // Create ResourceRequests table if it doesn't exist
            if (!tableExists(conn, "ResourceRequests")) {
                try (Statement stmt = conn.createStatement()) {
                    String createResourceRequestsTable =
                        "CREATE TABLE ResourceRequests (" +
                        "RequestID VARCHAR(36) PRIMARY KEY, " +
                        "OrphanageID VARCHAR(36), " +
                        "Category VARCHAR(50), " +
                        "Description VARCHAR(500), " +
                        "Needed INTEGER, " +
                        "Fulfilled INTEGER DEFAULT 0, " +
                        "Urgency VARCHAR(20), " +
                        "Status VARCHAR(20) DEFAULT 'OPEN'" +
                        ")";
                   
                    stmt.execute(createResourceRequestsTable);
                    logger.info("ResourceRequests table created successfully");
                }
            } else {
                logger.debug("ResourceRequests table already exists");
            }
            // Insert sample orphanage if table is empty
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Orphanages");
                if (rs.next() && rs.getInt(1) == 0) {
                    String insertSample =
                        "INSERT INTO Orphanages (OrphanageID, Name, Address, ContactEmail, Status) " +
                        "VALUES ('550e8400-e29b-41d4-a716-446655440001', " +
                        "'Hope Children Home', " +
                        "'123 Main Street, City', " +
                        "'contact@hopehome.org', " +
                        "'ACTIVE')";
                    stmt.execute(insertSample);
                    logger.info("Sample orphanage data inserted");
                }
            } catch (SQLException e) {
                logger.debug("Sample data may already exist or table is not empty");
            }
            initialized = true;
            logger.info("Database initialization completed successfully");
           
        } catch (SQLException e) {
            logger.error("Error initializing database connection: " + e.getMessage(), e);
            logger.error("Database URL: " + url);
        }
    }
    public static Connection getConnection() throws SQLException {
        String url = p.getProperty("db.url", "jdbc:ucanaccess://db/OrphanageHub.accdb;newDatabaseVersion=V2010");
       
        if (!initialized) {
            logger.warn("Database was not initialized properly, attempting to reinitialize");
            initializeDatabase();
        }
       
        try {
            Connection conn = DriverManager.getConnection(url);
            logger.debug("Database connection obtained");
            return conn;
        } catch (SQLException e) {
            logger.error("Failed to get database connection: " + e.getMessage(), e);
            throw new SQLException("Failed to connect to database. Please ensure the database is accessible.", e);
        }
    }
    public static boolean isInitialized() {
        return initialized;
    }
   
    // Test method to verify tables
    public static void verifyTables() {
        try (Connection conn = getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            String[] tables = {"Users", "Orphanages", "Donations", "VolunteerActivities", "ResourceRequests"};
           
            for (String table : tables) {
                if (tableExists(conn, table)) {
                    logger.info("Table {} exists", table);
                } else {
                    logger.warn("Table {} does not exist", table);
                }
            }
        } catch (SQLException e) {
            logger.error("Error verifying tables", e);
        }
    }
}