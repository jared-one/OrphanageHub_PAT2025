package com.orphanagehub.tools;

import com.orphanagehub.util.DatabaseManager;

import java.sql.Connection;
import java.sql.SQLException;

public class DbTest {
    public static void main(String[] args) {
        System.out.println("Testing database connection...");
        
        try (Connection conn = DatabaseManager.getConnection()) {
            System.out.println("✓ Connection successful");
            
            DatabaseManager.verifyTables();
            System.out.println("✓ Tables verified");
            
        } catch (SQLException e) {
            System.out.println("✗ Connection failed: " + e.getMessage());
        }
    }
}