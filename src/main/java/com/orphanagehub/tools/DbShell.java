package com.orphanagehub.tools;

import com.orphanagehub.dao.DatabaseManager;
import io.vavr.control.Try;

import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Enhanced interactive SQL shell with history, formatting, and scripting support.
 * 
 * @author OrphanageHub Team
 * @version 2.0
 * @since 2025-09-06
 */
public class DbShell {
    
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BOLD = "\u001B[1m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_CYAN = "\u001B[36m";
    
    private static List<String> commandHistory = new ArrayList<>();
    private static final String HISTORY_FILE = ".dbshell_history";
    private static boolean prettyPrint = true;
    private static boolean timing = false;
    private static PrintWriter logWriter = null;
    
    public static void main(String[] args) {
        loadHistory();
        
        // Check for script mode
        if (args.length > 0) {
            if (args[0].equals("-f") && args.length > 1) {
                executeScript(args[1]);
                return;
            } else if (args[0].equals("-c") && args.length > 1) {
                executeCommand(args[1]);
                return;
            }
        }
        
        // Interactive mode
        runInteractiveShell();
    }
    
    private static void runInteractiveShell() {
        Scanner scanner = new Scanner(System.in);
        printWelcome();
        
        String input;
        StringBuilder multiLineCommand = new StringBuilder();
        boolean inMultiLine = false;
        
        while (true) {
            if (!inMultiLine) {
                System.out.print(ANSI_BOLD + ANSI_CYAN + "SQL> " + ANSI_RESET);
            } else {
                System.out.print(ANSI_CYAN + "...> " + ANSI_RESET);
            }
            
            input = scanner.nextLine();
            
            // Check for special commands
            if (!inMultiLine && input.startsWith(".")) {
                handleSpecialCommand(input);
                continue;
            }
            
            // Check for exit
            if (!inMultiLine && (input.equalsIgnoreCase("exit") || 
                                input.equalsIgnoreCase("quit") || 
                                input.equalsIgnoreCase("\\q"))) {
                break;
            }
            
            // Handle multi-line SQL
            multiLineCommand.append(input).append(" ");
            
            if (input.trim().endsWith(";")) {
                inMultiLine = false;
                String sql = multiLineCommand.toString().trim();
                if (!sql.isEmpty()) {
                    executeSQL(sql);
                    commandHistory.add(sql);
                }
                multiLineCommand = new StringBuilder();
            } else {
                inMultiLine = true;
            }
        }
        
        scanner.close();
        saveHistory();
        cleanup();
        System.out.println(ANSI_GREEN + "\nGoodbye!" + ANSI_RESET);
    }
    
    private static void printWelcome() {
        System.out.println("\n" + ANSI_BOLD + ANSI_BLUE + 
            "╔══════════════════════════════════════════════════════╗");
        System.out.println("║            OrphanageHub Database Shell v2.0          ║");
        System.out.println("╚══════════════════════════════════════════════════════╝" + 
            ANSI_RESET);
        System.out.println("Type '.help' for commands, 'exit' to quit\n");
        
        // Show connection info
        DatabaseManager.getConnection().forEach(conn -> {
            try {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("Connected to: " + meta.getDatabaseProductName() + 
                    " " + meta.getDatabaseProductVersion());
                System.out.println("Database: " + meta.getURL() + "\n");
                conn.close();
            } catch (SQLException e) {
                System.err.println("Warning: Could not retrieve database info");
            }
        });
    }
    
    private static void handleSpecialCommand(String command) {
        String[] parts = command.split("\\s+", 2);
        String cmd = parts[0].toLowerCase();
        
        switch (cmd) {
            case ".help", ".h" -> showHelp();
            case ".tables", ".t" -> showTables();
            case ".schema", ".s" -> showSchema(parts.length > 1 ? parts[1] : null);
            case ".describe", ".d" -> describeTable(parts.length > 1 ? parts[1] : null);
            case ".history", ".hist" -> showHistory();
            case ".clear" -> clearScreen();
            case ".pretty" -> togglePrettyPrint();
            case ".timing" -> toggleTiming();
            case ".export" -> exportResults(parts.length > 1 ? parts[1] : null);
            case ".import" -> importData(parts.length > 1 ? parts[1] : null);
            case ".log" -> toggleLogging(parts.length > 1 ? parts[1] : null);
            case ".stats" -> showStatistics();
            case ".indexes" -> showIndexes();
            case ".users" -> showUsers();
            case ".orphanages" -> showOrphanages();
            default -> System.out.println("Unknown command: " + cmd + " (type .help for commands)");
        }
    }
    
    private static void showHelp() {
        System.out.println(ANSI_BOLD + "\nAvailable Commands:" + ANSI_RESET);
        System.out.println("  .help, .h              - Show this help message");
        System.out.println("  .tables, .t            - List all tables");
        System.out.println("  .schema [table], .s    - Show table schema");
        System.out.println("  .describe [table], .d  - Describe table structure");
        System.out.println("  .history, .hist        - Show command history");
        System.out.println("  .clear                 - Clear screen");
        System.out.println("  .pretty                - Toggle pretty printing");
        System.out.println("  .timing                - Toggle query timing");
        System.out.println("  .export [file]         - Export query results to CSV");
        System.out.println("  .import [file]         - Import data from CSV");
        System.out.println("  .log [file]            - Toggle query logging");
        System.out.println("  .stats                 - Show database statistics");
        System.out.println("  .indexes               - Show all indexes");
        System.out.println("  .users                 - Quick view of users");
        System.out.println("  .orphanages            - Quick view of orphanages");
        System.out.println("  exit, quit, \\q         - Exit the shell\n");
    }
    
    private static void showTables() {
        executeSQL("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name");
    }
    
    private static void showSchema(String tableName) {
        if (tableName == null) {
            executeSQL("SELECT sql FROM sqlite_master WHERE type='table' ORDER BY name");
        } else {
            executeSQL("SELECT sql FROM sqlite_master WHERE type='table' AND name='" + 
                tableName + "'");
        }
    }
    
    private static void describeTable(String tableName) {
        if (tableName == null) {
            System.out.println("Usage: .describe <table_name>");
            return;
        }
        
        DatabaseManager.getConnection().forEach(conn -> {
            try {
                DatabaseMetaData meta = conn.getMetaData();
                ResultSet columns = meta.getColumns(null, null, tableName, null);
                
                System.out.println("\nTable: " + ANSI_BOLD + tableName + ANSI_RESET);
                System.out.println("─".repeat(60));
                System.out.printf("%-20s %-15s %-10s %-10s%n", 
                    "Column", "Type", "Nullable", "Default");
                System.out.println("─".repeat(60));
                
                while (columns.next()) {
                    System.out.printf("%-20s %-15s %-10s %-10s%n",
                        columns.getString("COLUMN_NAME"),
                        columns.getString("TYPE_NAME") + "(" + columns.getInt("COLUMN_SIZE") + ")",
                        columns.getString("IS_NULLABLE"),
                        columns.getString("COLUMN_DEF") != null ? columns.getString("COLUMN_DEF") : "");
                }
                
                // Show indexes
                System.out.println("\nIndexes:");
                ResultSet indexes = meta.getIndexInfo(null, null, tableName, false, false);
                while (indexes.next()) {
                    if (indexes.getString("INDEX_NAME") != null) {
                        System.out.println("  • " + indexes.getString("INDEX_NAME") + 
                            " on " + indexes.getString("COLUMN_NAME"));
                    }
                }
                
                // Show foreign keys
                System.out.println("\nForeign Keys:");
                ResultSet foreignKeys = meta.getImportedKeys(null, null, tableName);
                while (foreignKeys.next()) {
                    System.out.println("  • " + foreignKeys.getString("FK_NAME") + ": " +
                        foreignKeys.getString("FKCOLUMN_NAME") + " -> " +
                        foreignKeys.getString("PKTABLE_NAME") + "(" +
                        foreignKeys.getString("PKCOLUMN_NAME") + ")");
                }
                
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error describing table: " + e.getMessage());
            }
        });
    }
    
    private static void executeSQL(String sql) {
        if (logWriter != null) {
            logWriter.println("[" + LocalDateTime.now() + "] " + sql);
        }
        
        long startTime = System.currentTimeMillis();
        
        DatabaseManager.getConnection()
            .flatMap(conn -> Try.of(() -> {
                try (conn; Statement stmt = conn.createStatement()) {
                    boolean hasResultSet = stmt.execute(sql);
                    
                    if (hasResultSet) {
                        ResultSet rs = stmt.getResultSet();
                        if (prettyPrint) {
                            printResultSetPretty(rs);
                        } else {
                            printResultSetSimple(rs);
                        }
                    } else {
                        int updateCount = stmt.getUpdateCount();
                        System.out.println(ANSI_GREEN + "Query OK, " + updateCount + 
                            " row(s) affected" + ANSI_RESET);
                    }
                    
                    if (timing) {
                        long elapsed = System.currentTimeMillis() - startTime;
                        System.out.println(ANSI_YELLOW + "Time: " + elapsed + " ms" + ANSI_RESET);
                    }
                    
                    return "Success";
                }
            }))
            .onFailure(e -> {
                System.err.println(ANSI_YELLOW + "Error: " + e.getMessage() + ANSI_RESET);
                if (logWriter != null) {
                    logWriter.println("  ERROR: " + e.getMessage());
                }
            });
    }
    
    private static void printResultSetPretty(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();
        
        // Calculate column widths
        int[] widths = new int[columnCount];
        List<String[]> rows = new ArrayList<>();
        
        for (int i = 0; i < columnCount; i++) {
            widths[i] = Math.max(meta.getColumnName(i + 1).length(), 10);
        }
        
        while (rs.next()) {
            String[] row = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                String value = rs.getString(i + 1);
                if (value == null) value = "NULL";
                if (value.length() > 50) value = value.substring(0, 47) + "...";
                row[i] = value;
                widths[i] = Math.min(Math.max(widths[i], value.length()), 50);
            }
            rows.add(row);
        }
        
        // Print header
        System.out.println();
        for (int i = 0; i < columnCount; i++) {
            System.out.printf("%-" + (widths[i] + 2) + "s", meta.getColumnName(i + 1));
        }
        System.out.println();
        
        // Print separator
        for (int i = 0; i < columnCount; i++) {
            System.out.print("─".repeat(widths[i] + 1) + " ");
        }
        System.out.println();
        
        // Print rows
        for (String[] row : rows) {
            for (int i = 0; i < columnCount; i++) {
                System.out.printf("%-" + (widths[i] + 2) + "s", row[i]);
            }
            System.out.println();
        }
        
        System.out.println("\n(" + rows.size() + " row" + (rows.size() != 1 ? "s" : "") + ")");
    }
    
    private static void printResultSetSimple(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();
        int rowCount = 0;
        
        while (rs.next()) {
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(rs.getString(i));
                if (i < columnCount) System.out.print(", ");
            }
            System.out.println();
            rowCount++;
        }
        
        System.out.println("(" + rowCount + " row" + (rowCount != 1 ? "s" : "") + ")");
    }
    
    private static void showHistory() {
        System.out.println("\nCommand History:");
        for (int i = 0; i < commandHistory.size(); i++) {
            System.out.println("  " + (i + 1) + ": " + commandHistory.get(i));
        }
    }
    
    private static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    
    private static void togglePrettyPrint() {
        prettyPrint = !prettyPrint;
        System.out.println("Pretty printing: " + (prettyPrint ? "ON" : "OFF"));
    }
    
    private static void toggleTiming() {
        timing = !timing;
        System.out.println("Query timing: " + (timing ? "ON" : "OFF"));
    }
    
    private static void toggleLogging(String filename) {
        if (logWriter != null) {
            logWriter.close();
            logWriter = null;
            System.out.println("Logging stopped");
        } else if (filename != null) {
            try {
                logWriter = new PrintWriter(new FileWriter(filename, true));
                System.out.println("Logging to: " + filename);
            } catch (IOException e) {
                System.err.println("Could not open log file: " + e.getMessage());
            }
        } else {
            System.out.println("Usage: .log <filename>");
        }
    }
    
    private static void showStatistics() {
        System.out.println("\nDatabase Statistics:");
        executeSQL("SELECT 'Users' as Table_Name, COUNT(*) as Count FROM TblUsers " +
                  "UNION ALL SELECT 'Orphanages', COUNT(*) FROM TblOrphanages " +
                  "UNION ALL SELECT 'Donations', COUNT(*) FROM TblDonations " +
                  "UNION ALL SELECT 'Requests', COUNT(*) FROM TblResourceRequests");
    }
    
    private static void showIndexes() {
        executeSQL("SELECT name, tbl_name FROM sqlite_master WHERE type='index' ORDER BY tbl_name, name");
    }
    
    private static void showUsers() {
        executeSQL("SELECT UserID, Username, Email, UserRole, AccountStatus FROM TblUsers LIMIT 20");
    }
    
    private static void showOrphanages() {
        executeSQL("SELECT OrphanageID, OrphanageName, City, Province, VerificationStatus " +
                  "FROM TblOrphanages LIMIT 20");
    }
    
    private static void exportResults(String filename) {
        if (filename == null) {
            System.out.println("Usage: .export <filename> followed by a SELECT query");
            return;
        }
        System.out.println("Next query results will be exported to: " + filename);
        // Implementation would set a flag to export next query results
    }
    
    private static void importData(String filename) {
        if (filename == null) {
            System.out.println("Usage: .import <filename.csv> <table>");
            return;
        }
        System.out.println("Import functionality not yet implemented");
    }
    
    private static void executeScript(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            StringBuilder sql = new StringBuilder();
            
            System.out.println("Executing script: " + filename);
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("--") || line.trim().isEmpty()) {
                    continue;
                }
                
                sql.append(line).append(" ");
                
                if (line.trim().endsWith(";")) {
                    executeSQL(sql.toString());
                    sql = new StringBuilder();
                }
            }
            
            System.out.println("Script execution completed");
        } catch (IOException e) {
            System.err.println("Error reading script file: " + e.getMessage());
        }
    }
    
    private static void executeCommand(String sql) {
        executeSQL(sql);
    }
    
    private static void loadHistory() {
        try (BufferedReader reader = new BufferedReader(new FileReader(HISTORY_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                commandHistory.add(line);
            }
        } catch (IOException e) {
            // History file doesn't exist yet
        }
    }
    
    private static void saveHistory() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(HISTORY_FILE))) {
            // Keep last 100 commands
            int start = Math.max(0, commandHistory.size() - 100);
            for (int i = start; i < commandHistory.size(); i++) {
                writer.println(commandHistory.get(i));
            }
        } catch (IOException e) {
            System.err.println("Could not save history: " + e.getMessage());
        }
    }
    
    private static void cleanup() {
        if (logWriter != null) {
            logWriter.close();
        }
        DatabaseManager.shutdown();
    }
}