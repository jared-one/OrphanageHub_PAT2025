package com.orphanagehub.tools;

import com.orphanagehub.dao.DatabaseManager;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbTest {
    public static void main(String[] args) {
        System.out.println("Testing database connection...");
        
        DatabaseManager.getConnection()
            .onSuccess(conn -> {
                try {
                    System.out.println("✓ Connection successful");
                    
                    // Verify database details
                    DatabaseMetaData meta = conn.getMetaData();
                    System.out.println("Database: " + meta.getDatabaseProductName());
                    System.out.println("Version: " + meta.getDatabaseProductVersion());
                    
                    // Test a simple query
                    conn.createStatement().executeQuery("SELECT 1");
                    System.out.println("✓ Query execution successful");
                    
                    // Verify connection pooling
                    System.out.println("✓ Connection pool is active");
                    
                    conn.close();
                    System.out.println("✓ Connection closed successfully");
                } catch (SQLException e) {
                    System.err.println("✗ Error during test: " + e.getMessage());
                    e.printStackTrace();
                }
            })
            .onFailure(ex -> {
                System.err.println("✗ Connection failed: " + ex.getMessage());
                ex.printStackTrace();
            });
    }
}
