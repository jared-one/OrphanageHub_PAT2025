/* Copyright (C) 2025 Jared Wisdom - All Rights Reserved */
package com.orphanagehub.tools;

import com.orphanagehub.dao.DatabaseManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbTest {
    public static void main(String[] args) {
        System.out.println("Testing database connection...");
        
        try (Connection conn = DatabaseManager.getConnection()) {
            System.out.println("✓ Connection successful");
            
            DatabaseManager.verifyTables();
            
            // Test query
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Users");
                if (rs.next()) {
                    System.out.println("Users table has " + rs.getInt(1) + " records");
                }
            }
            
        } catch (Exception e) {
            System.err.println("✗ Database test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
