package com.orphanagehub.tools;

// COMPLETE IMPORTS
import com.orphanagehub.dao.*;
import com.orphanagehub.model.*;
import com.orphanagehub.util.PasswordUtil;
import io.vavr.control.Try;
import com.orphanagehub.util.PasswordUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Comprehensive database testing tool with performance benchmarks.
 */
public class DbTest {
    
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    
    private static int totalTests = 0;
    private static int passedTests = 0;
    private static List<TestResult> testResults = new ArrayList<>();
    
    public static void main(String[] args) {
        printHeader();
        
        // Parse arguments
        boolean verbose = false;
        boolean performance = false;
        boolean stress = false;
        System.out.println(PasswordUtil.hash("Admin: Admin@2025!"));
        for (String arg : args) {
            switch (arg) {
                case "--verbose", "-v" -> verbose = true;
                case "--performance", "-p" -> performance = true;
                case "--stress", "-s" -> stress = true;
                case "--all", "-a" -> {
                    verbose = true;
                    performance = true;
                    stress = true;
                }
            }
        }
        
        // Run tests
        testBasicConnectivity();
        testConnectionPooling();
        testTransactions();
        testCRUDOperations();
        testRelationships();
        testConcurrency();
        
        if (performance) {
            runPerformanceTests();
        }
        
        if (stress) {
            runStressTests();
        }
        
        printSummary(verbose);
        
        // Shutdown
        DatabaseManager.shutdown();
        System.exit(passedTests == totalTests ? 0 : 1);
    }
    
    private static void printHeader() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║             DATABASE TEST SUITE - Comprehensive Testing       ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");
    }
    
    private static void testBasicConnectivity() {
        System.out.println(ANSI_BLUE + "[1] BASIC CONNECTIVITY TESTS" + ANSI_RESET);
        System.out.println("════════════════════════════════════════");
        
        // Test 1: Simple connection
        runTest("Basic Connection", () -> {
            Try<Connection> conn = DatabaseManager.getConnection();
            if (conn.isSuccess()) {
                conn.get().close();
                return true;
            }
            return false;
        });
        
        // Test 2: Connection validity
        runTest("Connection Validity", () -> {
            Try<Connection> conn = DatabaseManager.getConnection();
            if (conn.isSuccess()) {
                boolean valid = conn.get().isValid(5);
                conn.get().close();
                return valid;
            }
            return false;
        });
        
        // Test 3: Database metadata
        runTest("Database Metadata", () -> {
            return DatabaseManager.getConnection()
                .map(conn -> {
                    try {
                        DatabaseMetaData meta = conn.getMetaData();
                        String product = meta.getDatabaseProductName();
                        conn.close();
                        return product != null && !product.isEmpty();
                    } catch (SQLException e) {
                        return false;
                    }
                }).getOrElse(false);
        });
        
        // Test 4: Simple query
        runTest("Simple Query", () -> {
            return DatabaseManager.getConnection()
                .map(conn -> {
                    try (Statement stmt = conn.createStatement()) {
                        ResultSet rs = stmt.executeQuery("SELECT 1");
                        boolean hasResult = rs.next() && rs.getInt(1) == 1;
                        conn.close();
                        return hasResult;
                    } catch (SQLException e) {
                        return false;
                    }
                }).getOrElse(false);
        });
        
        System.out.println();
    }
    
    private static void testConnectionPooling() {
        System.out.println(ANSI_BLUE + "[2] CONNECTION POOLING TESTS" + ANSI_RESET);
        System.out.println("════════════════════════════════════════");
        
        // Test 1: Multiple connections
        runTest("Multiple Connections", () -> {
            List<Connection> connections = new ArrayList<>();
            try {
                for (int i = 0; i < 5; i++) {
                    Try<Connection> conn = DatabaseManager.getConnection();
                    if (conn.isSuccess()) {
                        connections.add(conn.get());
                    } else {
                        return false;
                    }
                }
                
                // All connections should be valid
                for (Connection conn : connections) {
                    if (!conn.isValid(1)) {
                        return false;
                    }
                }
                
                // Close all
                for (Connection conn : connections) {
                    conn.close();
                }
                
                return true;
            } catch (SQLException e) {
                return false;
            }
        });
        
        // Test 2: Connection reuse
        runTest("Connection Reuse", () -> {
            try {
                Connection conn1 = DatabaseManager.getConnection().get();
                conn1.close();
                
                // Should be able to get another connection immediately
                Connection conn2 = DatabaseManager.getConnection().get();
                boolean valid = conn2.isValid(1);
                conn2.close();
                
                return valid;
            } catch (Exception e) {
                return false;
            }
        });
        
        // Test 3: Pool exhaustion recovery
        runTest("Pool Recovery", () -> {
            try {
                // Get max connections
                List<Connection> connections = new ArrayList<>();
                for (int i = 0; i < 10; i++) {
                    connections.add(DatabaseManager.getConnection().get());
                }
                
                // Close half
                for (int i = 0; i < 5; i++) {
                    connections.get(i).close();
                }
                
                // Should be able to get more
                Connection newConn = DatabaseManager.getConnection().get();
                boolean valid = newConn.isValid(1);
                
                // Cleanup
                newConn.close();
                for (int i = 5; i < 10; i++) {
                    connections.get(i).close();
                }
                
                return valid;
            } catch (Exception e) {
                return false;
            }
        });
        
        System.out.println();
    }
    
    private static void testTransactions() {
        System.out.println(ANSI_BLUE + "[3] TRANSACTION TESTS" + ANSI_RESET);
        System.out.println("════════════════════════════════════════");
        
        // Test 1: Basic transaction
        runTest("Basic Transaction", () -> {
            return DatabaseManager.getConnection()
                .map(conn -> {
                    try {
                        conn.setAutoCommit(false);
                        
                        // Create temp table
                        Statement stmt = conn.createStatement();
                        stmt.execute("CREATE TEMP TABLE test_trans (id INTEGER, value TEXT)");
                        stmt.execute("INSERT INTO test_trans VALUES (1, 'test')");
                        
                        conn.commit();
                        
                        // Verify data
                        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM test_trans");
                        boolean success = rs.next() && rs.getInt(1) == 1;
                        
                        conn.close();
                        return success;
                    } catch (SQLException e) {
                        try { conn.rollback(); } catch (SQLException ex) {}
                        return false;
                    }
                }).getOrElse(false);
        });
        
        System.out.println();
    }
    
    private static void testCRUDOperations() {
        System.out.println(ANSI_BLUE + "[4] CRUD OPERATION TESTS" + ANSI_RESET);
        System.out.println("════════════════════════════════════════");
        
        UserDAO userDAO = new UserDAO();
        OrphanageDAO orphanageDAO = new OrphanageDAO();
        DonationDAO donationDAO = new DonationDAO();
        
        // Test 1: User CRUD
        runTest("User CRUD", () -> {
            String testUsername = "test_" + System.currentTimeMillis();
            
            // Create
            User testUser = User.createBasic(
                null, testUsername, PasswordUtil.hash("Test123!"),
                testUsername + "@test.com", "Donor"
            );
            
            Try<User> created = userDAO.create(testUser);
            if (created.isFailure()) return false;
            
            Integer userId = created.get().userId();
            
            // Read
            Try<io.vavr.control.Option<User>> found = userDAO.findById(userId);
            if (found.isFailure() || found.get().isEmpty()) return false;
            
            // Update
            User updated = found.get().get().withAccountStatus("Suspended");
            Try<Void> updateResult = userDAO.update(updated);
            if (updateResult.isFailure()) return false;
            
            // Verify update
            Try<io.vavr.control.Option<User>> verified = userDAO.findById(userId);
            if (verified.isFailure() || verified.get().isEmpty()) return false;
            
            boolean statusUpdated = "Suspended".equals(verified.get().get().accountStatus());
            
            return statusUpdated;
        });
        
        System.out.println();
    }
    
    private static void testRelationships() {
        System.out.println(ANSI_BLUE + "[5] RELATIONSHIP TESTS" + ANSI_RESET);
        System.out.println("════════════════════════════════════════");
        
        // Test 1: Foreign key constraints
        runTest("Foreign Key Constraints", () -> {
            return DatabaseManager.getConnection()
                .map(conn -> {
                    try {
                        Statement stmt = conn.createStatement();
                        
                        // Try to insert donation with non-existent user
                        // This should fail due to foreign key constraint
                        try {
                            stmt.execute("INSERT INTO TblDonations (DonorID, OrphanageID, DonationType, Status) " +
                                       "VALUES (999999, 1, 'Money', 'Pending')");
                            return false; // Should not reach here
                        } catch (SQLException e) {
                            // Expected - foreign key violation
                        }
                        
                        conn.close();
                        return true;
                    } catch (SQLException e) {
                        return false;
                    }
                }).getOrElse(false);
        });
        
        System.out.println();
    }
    
    private static void testConcurrency() {
        System.out.println(ANSI_BLUE + "[6] CONCURRENCY TESTS" + ANSI_RESET);
        System.out.println("════════════════════════════════════════");
        
        // Test 1: Concurrent reads
        runTest("Concurrent Reads", () -> {
            ExecutorService executor = Executors.newFixedThreadPool(5);
            CountDownLatch latch = new CountDownLatch(5);
            AtomicBoolean success = new AtomicBoolean(true);
            
            for (int i = 0; i < 5; i++) {
                executor.submit(() -> {
                    try {
                        Connection conn = DatabaseManager.getConnection().get();
                        Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM TblUsers");
                        if (!rs.next()) {
                            success.set(false);
                        }
                        conn.close();
                    } catch (Exception e) {
                        success.set(false);
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            try {
                latch.await(10, TimeUnit.SECONDS);
                executor.shutdown();
                return success.get();
            } catch (InterruptedException e) {
                return false;
            }
        });
        
        System.out.println();
    }
    
    private static void runPerformanceTests() {
        System.out.println(ANSI_BLUE + "[7] PERFORMANCE TESTS" + ANSI_RESET);
        System.out.println("════════════════════════════════════════");
        
        // Test 1: Insert performance
        runTest("Insert Performance (100 records)", () -> {
            UserDAO userDAO = new UserDAO();
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < 100; i++) {
                String username = "perf_" + System.currentTimeMillis() + "_" + i;
                User user = User.createBasic(
                    null, username, PasswordUtil.hash("Test123!"),
                    username + "@test.com", "Donor"
                );
                
                Try<User> result = userDAO.create(user);
                if (result.isFailure()) {
                    return false;
                }
            }
            
            long elapsed = System.currentTimeMillis() - startTime;
            System.out.println("    Time: " + elapsed + "ms (" + (100000.0/elapsed) + " ops/sec)");
            
            return elapsed < 5000; // Should complete in under 5 seconds
        });
        
        System.out.println();
    }
    
    private static void runStressTests() {
        System.out.println(ANSI_BLUE + "[8] STRESS TESTS" + ANSI_RESET);
        System.out.println("════════════════════════════════════════");
        
        // Test 1: Connection stress
        runTest("Connection Stress (100 rapid connections)", () -> {
            for (int i = 0; i < 100; i++) {
                Try<Connection> conn = DatabaseManager.getConnection();
                if (conn.isFailure()) {
                    return false;
                }
                try {
                    conn.get().close();
                } catch (SQLException e) {
                    return false;
                }
            }
            return true;
        });
        
        System.out.println();
    }
    
    private static void runTest(String testName, TestCase test) {
        totalTests++;
        long startTime = System.currentTimeMillis();
        
        try {
            boolean result = test.run();
            long elapsed = System.currentTimeMillis() - startTime;
            
            if (result) {
                System.out.println(ANSI_GREEN + "  ✓ " + testName + 
                    " (" + elapsed + "ms)" + ANSI_RESET);
                passedTests++;
                testResults.add(new TestResult(testName, true, elapsed, null));
            } else {
                System.out.println(ANSI_RED + "  ✗ " + testName + 
                    " (" + elapsed + "ms)" + ANSI_RESET);
                testResults.add(new TestResult(testName, false, elapsed, "Test returned false"));
            }
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            System.out.println(ANSI_RED + "  ✗ " + testName + 
                " - Exception: " + e.getMessage() + " (" + elapsed + "ms)" + ANSI_RESET);
            testResults.add(new TestResult(testName, false, elapsed, e.getMessage()));
        }
    }
    
    private static void printSummary(boolean verbose) {
        System.out.println(ANSI_BLUE + "\n[TEST SUMMARY]" + ANSI_RESET);
        System.out.println("════════════════════════════════════════");
        
        double successRate = totalTests > 0 ? (passedTests * 100.0 / totalTests) : 0;
        
        System.out.println("  Total Tests: " + totalTests);
        System.out.println("  Passed: " + ANSI_GREEN + passedTests + ANSI_RESET);
        System.out.println("  Failed: " + ANSI_RED + (totalTests - passedTests) + ANSI_RESET);
        System.out.println("  Success Rate: " + 
            (successRate >= 80 ? ANSI_GREEN : successRate >= 50 ? ANSI_YELLOW : ANSI_RED) +
            String.format("%.1f%%", successRate) + ANSI_RESET);
        
        if (verbose) {
            System.out.println("\n  Failed Tests:");
            testResults.stream()
                .filter(r -> !r.passed)
                .forEach(r -> System.out.println("    • " + r.testName + 
                    (r.error != null ? " - " + r.error : "")));
            
            System.out.println("\n  Slowest Tests:");
            testResults.stream()
                .sorted((a, b) -> Long.compare(b.duration, a.duration))
                .limit(5)
                .forEach(r -> System.out.println("    • " + r.testName + 
                    " (" + r.duration + "ms)"));
        }
        
        System.out.println("\n" + (passedTests == totalTests ? 
            ANSI_GREEN + "  ✓ ALL TESTS PASSED!" : 
            ANSI_RED + "  ✗ SOME TESTS FAILED") + ANSI_RESET);
    }
    
    @FunctionalInterface
    private interface TestCase {
        boolean run() throws Exception;
    }
    
    private static class TestResult {
        final String testName;
        final boolean passed;
        final long duration;
        final String error;
        
        TestResult(String testName, boolean passed, long duration, String error) {
            this.testName = testName;
            this.passed = passed;
            this.duration = duration;
            this.error = error;
        }
    }
}