package com.orphanagehub.tools;

import com.orphanagehub.dao.DatabaseManager;
import io.vavr.control.Try;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

/**
 * CLI shell for executing SQL queries.
 * Interactive mode for dev.
 */
public class DbShell {

    /**
     * Executes a SQL query.
     * @param sql The SQL statement.
     * @return Try<String> - result set as string on success.
     */
    public static Try<String> executeQuery(String sql) {
        return DatabaseManager.getConnection()
                .flatMap(conn -> Try.of(() -> {
                    try (conn; Statement stmt = conn.createStatement()) {
                        boolean hasResultSet = stmt.execute(sql);
                        if (hasResultSet) {
                            ResultSet rs = stmt.getResultSet();
                            StringBuilder sb = new StringBuilder();
                            int columnCount = rs.getMetaData().getColumnCount();
                            while (rs.next()) {
                                for (int i = 1; i <= columnCount; i++) {
                                    sb.append(rs.getString(i));
                                    if (i < columnCount) sb.append(", ");
                                }
                                sb.append("\n");
                            }
                            return sb.toString();
                        } else {
                            return "Update count: " + stmt.getUpdateCount();
                        }
                    }
                }));
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Database Shell - Enter SQL queries (type 'exit' to quit):");
        String sql;
        while (true) {
            System.out.print("SQL> ");
            sql = scanner.nextLine();
            if (sql.equalsIgnoreCase("exit")) {
                break;
            }
            executeQuery(sql)
                .onSuccess(System.out::println)
                .onFailure(e -> System.err.println("Error: " + e.getMessage()));
        }
        scanner.close();
        System.out.println("Goodbye!");
    }
}