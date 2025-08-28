/* Copyright (C) 2025 Jared Wisdom - All Rights Reserved */
package com.orphanagehub.tools;

import com.orphanagehub.dao.DatabaseManager;
import java.sql.*;

public class DbShell {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: make db:sql q=\"YOUR_QUERY\"");
            System.exit(1);
        }
        String query = args[0];
        System.out.println("Executing: " + query);
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            boolean hasResultSet = stmt.execute(query);
            if (hasResultSet) {
                try (ResultSet rs = stmt.getResultSet()) {
                    ResultSetMetaData meta = rs.getMetaData();
                    int colCount = meta.getColumnCount();
                    for (int i = 1; i <= colCount; i++) {
                        System.out.printf("%-25s", meta.getColumnName(i));
                    }
                    System.out.println("\n" + "-".repeat(colCount * 25));
                    while (rs.next()) {
                        for (int i = 1; i <= colCount; i++) {
                            System.out.printf("%-25s", rs.getString(i));
                        }
                        System.out.println();
                    }
                }
            } else {
                System.out.println("Query OK, " + stmt.getUpdateCount() + " rows affected.");
            }
        } catch (SQLException e) {
            System.err.println("Query failed: " + e.getMessage());
            System.exit(1);
        }
    }
}
