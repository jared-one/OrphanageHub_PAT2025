package com.orphanagehub.tools;

import com.orphanagehub.util.DatabaseManager;

import java.sql.Connection;
import java.sql.SQLException;

public class DbDoctor {
    public static void main(String[] args) {
        System.out.println("🩺 Checking database connectivity...");
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println(
                        "\n✅ SUCCESS: Connection to the database was established successfully.");
            } else {
                System.out.println("\n❌ ERROR: Failed to connect to the database.");
            }
        } catch (SQLException e) {
            System.out.println("\n❌ ERROR: Database connection failed.");
            e.printStackTrace();
        }
    }
}