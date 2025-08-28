package com.orphanagehub.tools;

import com.orphanagehub.util.DatabaseManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DbShell {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java DbShell \"YOUR_SQL_QUERY\"");
            return;
        }
        String query = args[0];
        System.out.println("Executing: " + query);
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            boolean hasResultSet = stmt.execute(query);
            if (hasResultSet) {
                ResultSet rs = stmt.getResultSet();
                while (rs.next()) {
                    // Print first column as example
                    System.out.println(rs.getString(1));
                }
            } else {
                System.out.println("Update count: " + stmt.getUpdateCount());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}