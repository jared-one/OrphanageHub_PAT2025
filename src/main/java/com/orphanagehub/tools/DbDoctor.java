/* Copyright (C) 2025 Jared Wisdom - All Rights Reserved */
package com.orphanagehub.tools;

/* Copyright (C) 2025 Jared Wisdom - All Rights Reserved */

import com.orphanagehub.dao.DatabaseManager;
import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbDoctor {
    private static final Logger logger = LoggerFactory.getLogger(DbDoctor.class);

    public static void main(String[] args) {
        System.out.println("🩺 Checking database connectivity...");
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println(
                        "\n✅ SUCCESS: Connection to the database was established successfully.");
            } else {
                System.out.println(
                        "\n❌ FAILED: Connection returned null or was immediately closed.");
            }
        } catch (Exception e) {
            System.err.println("\n❌ FAILED: Could not connect to the database.");
            System.err.println("   Error Type: " + e.getClass().getSimpleName());
            System.err.println("   Message: " + e.getMessage());
            System.exit(1);
        }
    }
}
