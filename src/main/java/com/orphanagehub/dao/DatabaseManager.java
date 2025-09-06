package com.orphanagehub.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages database connections using HikariCP pooling with automatic schema management.
 * Provides efficient, thread-safe connections with database initialization.
 */
public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String CONFIG_FILE = "/app.properties";  // Align with resources
    private static final String DEFAULT_DB_PATH = "db/OrphanageHub.accdb";
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    
    private static volatile HikariDataSource dataSource;
    
    static {
        initializeDataSource();
    }
    
    /**
     * Initialize the HikariCP data source with proper configuration
     */
    private static void initializeDataSource() {
        Try.of(() -> {
            HikariConfig config = new HikariConfig();
            
            // Load configuration from properties file, with fallback to defaults
            Properties props = loadDatabaseProperties();
            
            // Configure HikariCP for Microsoft Access via UCanAccess
            String dbPath = props.getProperty("db.url", "jdbc:ucanaccess://" + DEFAULT_DB_PATH);
            config.setJdbcUrl(dbPath + ";immediatelyReleaseResources=true;memory=false;openExclusive=false");
            // Try both possible driver class names (different versions use different packages)
            try {
                Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
                config.setDriverClassName("net.ucanaccess.jdbc.UcanaccessDriver");
            } catch (ClassNotFoundException e1) {
                try {
                    Class.forName("net.sf.ucanaccess.jdbc.UcanaccessDriver");
                    config.setDriverClassName("net.sf.ucanaccess.jdbc.UcanaccessDriver");
                } catch (ClassNotFoundException e2) {
                    throw new RuntimeException("UCanAccess driver not found in classpath", e2);
                }
            }
            
            // Connection pool settings optimized for small desktop app
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("project.build.pool.maxSize", "10")));
            config.setMinimumIdle(Integer.parseInt(props.getProperty("project.build.pool.minIdle", "2")));
            config.setConnectionTimeout(Long.parseLong(props.getProperty("project.build.pool.connectionTimeout", "30000")));
            config.setIdleTimeout(Long.parseLong(props.getProperty("project.build.pool.idleTimeout", "600000")));
            config.setMaxLifetime(Long.parseLong(props.getProperty("project.build.pool.maxLifetime", "1800000")));
            
            // Connection test query for Access/HSQLDB - Use VALUES(1) instead of SELECT 1
            config.setConnectionTestQuery("VALUES(1)");
            config.setPoolName("OrphanageHubPool");
            
            // Additional optimizations for desktop use
            config.setAutoCommit(true);
            config.setReadOnly(false);
            config.setLeakDetectionThreshold(60000);
            
            dataSource = new HikariDataSource(config);
            logger.info("HikariCP connection pool initialized successfully");
            
            // Initialize database schema after pool creation
            initializeDatabase();
            return null;
        }).onFailure(error -> {
            logger.error("Failed to initialize database connection pool", error);
            throw new RuntimeException("Database initialization failed: " + error.getMessage(), error);
        });
    }
    
    /**
     * Load database properties from configuration file
     */
    private static Properties loadDatabaseProperties() {
        Properties props = new Properties();
        
        Try.of(() -> {
            try (InputStream is = DatabaseManager.class.getResourceAsStream(CONFIG_FILE)) {
                if (is != null) {
                    props.load(is);
                    logger.info("Database configuration loaded from {}", CONFIG_FILE);
                } else {
                    logger.warn("Configuration file {} not found, using defaults", CONFIG_FILE);
                    setDefaultProperties(props);
                }
            }
            return props;
        }).onFailure(error -> {
            logger.warn("Error loading database configuration, using defaults", error);
            setDefaultProperties(props);
        });
        
        return props;
    }
    
    /**
     * Set default database properties
     */
    private static void setDefaultProperties(Properties props) {
        props.setProperty("db.url", "jdbc:ucanaccess://" + DEFAULT_DB_PATH);
        props.setProperty("project.build.pool.maxSize", "10");
        props.setProperty("project.build.pool.minIdle", "2");
        props.setProperty("project.build.pool.connectionTimeout", "30000");
        props.setProperty("project.build.pool.idleTimeout", "600000");
        props.setProperty("project.build.pool.maxLifetime", "1800000");
    }
    
    /**
     * Gets a connection from the pool.
     * @return Try<Connection> - success with connection, failure on error
     */
    public static Try<Connection> getConnection() {
        if (dataSource == null || dataSource.isClosed()) {
            return Try.failure(new SQLException("Database connection pool is not initialized or closed"));
        }
        return Try.of(dataSource::getConnection);
    }
    
    /**
     * Initialize database schema - create/update tables and columns
     */
    private static void initializeDatabase() {
        if (!initialized.compareAndSet(false, true)) {
            return; // Already initialized
        }
        
        getConnection().andThen(conn -> {
            Try.run(() -> {
                // Ensure required columns exist in TblUsers
                ensureUserTableSchema(conn);
                
                // Verify all tables exist
                verifyRequiredTables(conn);
                
                logger.info("Database schema initialization completed successfully");
            }).onFailure(error -> {
                logger.error("Failed to initialize database schema", error);
                initialized.set(false); // Reset to allow retry
            });
        });
    }

    /**
     * Ensure TblUsers has all required columns with proper schema
     */
    private static void ensureUserTableSchema(Connection conn) {
        // Check and add FullName column if missing
        Try.run(() -> {
            if (!columnExists(conn, "TblUsers", "FullName")) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE TblUsers ADD COLUMN FullName TEXT(100)");
                    logger.info("Added FullName column to TblUsers");
                }
            }
        }).onFailure(error -> logger.error("Failed to add FullName column", error));
        
        // Check and add AccountStatus column if missing
        Try.run(() -> {
            if (!columnExists(conn, "TblUsers", "AccountStatus")) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE TblUsers ADD COLUMN AccountStatus TEXT(20) DEFAULT 'Active'");
                    logger.info("Added AccountStatus column to TblUsers");
                }
            }
        }).onFailure(error -> logger.error("Failed to add AccountStatus column", error));
        
        // Additional columns can be added here as per project needs
    }

    /**
     * Verify all required tables exist in the database
     */
    private static void verifyRequiredTables(Connection conn) {
        DatabaseMetaData meta = Try.of(conn::getMetaData).getOrElseThrow((e) -> new RuntimeException(e));
        
        // List of required tables from project scope
        String[] requiredTables = {
            "TblUsers", "TblOrphanages", "TblResourceRequests"
        };
        
        for (String tableName : requiredTables) {
            Try.run(() -> {
                try (ResultSet tables = meta.getTables(null, null, tableName, null)) {
                    if (!tables.next()) {
                        logger.warn("Required table {} does not exist in database", tableName);
                    } else {
                        logger.debug("Verified table {} exists", tableName);
                    }
                }
            }).onFailure(error -> logger.error("Failed to verify table " + tableName, error));
        }
        
        // Verify required columns in TblUsers
        String[] requiredUserColumns = {
            "UserID", "Username", "PasswordHash", "Email", "UserRole", "DateRegistered", "FullName", "AccountStatus"
        };
        
        for (String column : requiredUserColumns) {
            Try.run(() -> {
                if (!columnExists(conn, "TblUsers", column)) {
                    logger.warn("Missing required column in TblUsers: {}", column);
                }
            }).onFailure(error -> logger.error("Failed to verify column " + column, error));
        }
    }

    /**
     * Check if a column exists in a table
     */
    private static boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet columns = meta.getColumns(null, null, tableName, columnName)) {
            return columns.next();
        }
    }
    
    /**
     * Test database connection
     */
    public static Try<Boolean> testConnection() {
        return getConnection().map(conn -> {
            try (conn) {
                return conn.isValid(5);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Shutdown the connection pool gracefully
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("Shutting down database connection pool");
            dataSource.close();
        }
    }
}