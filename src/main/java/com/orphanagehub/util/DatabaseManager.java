package com.orphanagehub.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database connection manager using HikariCP connection pool.
 */
public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static HikariDataSource dataSource;
    private static final String DB_PATH = "db/OrphanageHub.sqlite";
    
    static {
        initialize();
    }
    
    private static void initialize() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + DB_PATH);
        config.setDriverClassName("org.sqlite.JDBC");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(300000);
        config.setConnectionTimeout(30000);
        config.setLeakDetectionThreshold(60000);
        
        // SQLite specific settings
        config.addDataSourceProperty("journal_mode", "WAL");
        config.addDataSourceProperty("synchronous", "NORMAL");
        config.addDataSourceProperty("temp_store", "MEMORY");
        config.addDataSourceProperty("mmap_size", "30000000000");
        
        try {
            dataSource = new HikariDataSource(config);
            logger.info("Database connection pool initialized");
        } catch (Exception e) {
            logger.error("Failed to initialize database connection pool", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    /**
     * Gets a connection from the pool.
     */
    public static Try<Connection> getConnection() {
        return Try.of(() -> {
            if (dataSource == null) {
                initialize();
            }
            return dataSource.getConnection();
        });
    }
    
    /**
     * Shuts down the connection pool.
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool shut down");
        }
    }
    
    /**
     * Checks if the database is available.
     */
    public static boolean isAvailable() {
        return Try.of(() -> {
            try (Connection conn = dataSource.getConnection()) {
                return conn.isValid(2);
            }
        }).getOrElse(false);
    }
}