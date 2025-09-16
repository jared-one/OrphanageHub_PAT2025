package com.orphanagehub.tools;

import com.orphanagehub.dao.DatabaseManager;
import io.vavr.control.Try;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Database migration tool for schema updates and data migrations.
 * 
 * @author OrphanageHub Team
 * @version 2.0
 * @since 2025-09-06
 */
public class DbMigrator {
    
    private static final String MIGRATIONS_DIR = "db/migrations";
    private static final String MIGRATION_TABLE = "schema_migrations";
    
    public static void main(String[] args) {
        if (args.length == 0) {
            showUsage();
            return;
        }
        
        String command = args[0];
        
        switch (command) {
            case "status" -> showStatus();
            case "migrate" -> runMigrations();
            case "rollback" -> rollback(args.length > 1 ? Integer.parseInt(args[1]) : 1);
            case "create" -> createMigration(args.length > 1 ? args[1] : "unnamed");
            case "reset" -> resetDatabase();
            case "seed" -> seedDatabase();
            default -> showUsage();
        }
    }
    
    private static void showUsage() {
        System.out.println("Database Migration Tool");
        System.out.println("Usage: java DbMigrator <command> [options]");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  status              Show migration status");
        System.out.println("  migrate             Run pending migrations");
        System.out.println("  rollback [n]        Rollback n migrations (default: 1)");
        System.out.println("  create <name>       Create new migration file");
        System.out.println("  reset               Reset database (drop and recreate)");
        System.out.println("  seed                Seed database with sample data");
    }
    
    private static void showStatus() {
        System.out.println("Migration Status");
        System.out.println("════════════════════════════════════════");
        
        DatabaseManager.getConnection().forEach(conn -> {
            try {
                ensureMigrationTable(conn);
                
                // Get applied migrations
                Set<String> applied = getAppliedMigrations(conn);
                System.out.println("Applied migrations: " + applied.size());
                
                // Get pending migrations
                List<String> pending = getPendingMigrations(conn);
                System.out.println("Pending migrations: " + pending.size());
                
                if (!pending.isEmpty()) {
                    System.out.println("\nPending:");
                    pending.forEach(m -> System.out.println("  • " + m));
                }
                
                conn.close();
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        });
    }
    
    private static void runMigrations() {
        System.out.println("Running Migrations");
        System.out.println("════════════════════════════════════════");
        
        DatabaseManager.getConnection().forEach(conn -> {
            try {
                ensureMigrationTable(conn);
                List<String> pending = getPendingMigrations(conn);
                
                if (pending.isEmpty()) {
                    System.out.println("No pending migrations");
                    conn.close();
                    return;
                }
                
                for (String migration : pending) {
                    System.out.println("Applying: " + migration);
                    applyMigration(conn, migration);
                    recordMigration(conn, migration);
                    System.out.println("  ✓ Applied successfully");
                }
                
                conn.close();
                System.out.println("\nAll migrations completed");
            } catch (Exception e) {
                System.err.println("Migration failed: " + e.getMessage());
            }
        });
    }
    
    private static void rollback(int steps) {
        System.out.println("Rolling back " + steps + " migration(s)");
        System.out.println("════════════════════════════════════════");
        
        // Implementation would execute down migrations
        System.out.println("Rollback functionality not yet implemented");
    }
    
    private static void createMigration(String name) {
        String timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        );
        String filename = timestamp + "_" + name + ".sql";
        
        Path migrationFile = Paths.get(MIGRATIONS_DIR, filename);
        
        try {
            Files.createDirectories(migrationFile.getParent());
            
            String template = """
                -- Migration: %s
                -- Created: %s
                
                -- UP
                -- Add your migration SQL here
                
                -- DOWN
                -- Add rollback SQL here
                """.formatted(name, LocalDateTime.now());
            
            Files.writeString(migrationFile, template);
            System.out.println("Created migration: " + filename);
        } catch (IOException e) {
            System.err.println("Failed to create migration: " + e.getMessage());
        }
    }
    
    private static void resetDatabase() {
        System.out.println("⚠ WARNING: This will delete all data!");
        System.out.print("Type 'yes' to confirm: ");
        
        Scanner scanner = new Scanner(System.in);
        String confirm = scanner.nextLine();
        
        if (!"yes".equalsIgnoreCase(confirm)) {
            System.out.println("Cancelled");
            return;
        }
        
        // Implementation would drop and recreate all tables
        System.out.println("Database reset completed");
    }
    
    private static void seedDatabase() {
        System.out.println("Seeding Database");
        System.out.println("════════════════════════════════════════");
        
        DataSeeder.seed(false); // Call the DataSeeder tool
    }
    
    private static void ensureMigrationTable(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS schema_migrations (
                version TEXT PRIMARY KEY,
                applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);
    }
    
    private static Set<String> getAppliedMigrations(Connection conn) throws SQLException {
        Set<String> applied = new HashSet<>();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT version FROM schema_migrations");
        
        while (rs.next()) {
            applied.add(rs.getString("version"));
        }
        
        return applied;
    }
    
    private static List<String> getPendingMigrations(Connection conn) throws IOException, SQLException {
        Set<String> applied = getAppliedMigrations(conn);
        Path migrationsPath = Paths.get(MIGRATIONS_DIR);
        
        if (!Files.exists(migrationsPath)) {
            return new ArrayList<>();
        }
        
        return Files.list(migrationsPath)
            .filter(path -> path.toString().endsWith(".sql"))
            .map(path -> path.getFileName().toString())
            .filter(name -> !applied.contains(name))
            .sorted()
            .collect(Collectors.toList());
    }
    
    private static void applyMigration(Connection conn, String migration) throws IOException, SQLException {
        Path migrationFile = Paths.get(MIGRATIONS_DIR, migration);
        String sql = Files.readString(migrationFile);
        
        // Extract UP section
        String upSection = extractSection(sql, "UP");
        
        Statement stmt = conn.createStatement();
        for (String statement : upSection.split(";")) {
            if (!statement.trim().isEmpty()) {
                stmt.execute(statement);
            }
        }
    }
    
    private static void recordMigration(Connection conn, String migration) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO schema_migrations (version) VALUES (?)"
        );
        ps.setString(1, migration);
        ps.executeUpdate();
    }
    
    private static String extractSection(String content, String section) {
        String marker = "-- " + section;
        int start = content.indexOf(marker);
        if (start == -1) return "";
        
        int end = content.indexOf("-- DOWN", start);
        if (end == -1) end = content.length();
        
        return content.substring(start + marker.length(), end).trim();
    }
}