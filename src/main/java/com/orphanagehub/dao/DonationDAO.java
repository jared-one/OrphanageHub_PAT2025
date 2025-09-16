package com.orphanagehub.dao;

import com.orphanagehub.model.Donation;
import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

public class DonationDAO {
    private static final Logger logger = LoggerFactory.getLogger(DonationDAO.class);
    
    public Try<Donation> create(Donation donation) {
        return Try.of(() -> {
            String sql = "INSERT INTO TblDonations (DonorID, OrphanageID, RequestID, " +
                       "DonationType, Amount, Currency, ItemDescription, Status, " +
                       "DonationDate, CreatedDate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                
                ps.setInt(1, donation.donorId());
                ps.setInt(2, donation.orphanageId());
                ps.setObject(3, donation.requestId().getOrNull());
                ps.setString(4, donation.donationType());
                ps.setObject(5, donation.amount().getOrNull());
                ps.setString(6, donation.currency());
                ps.setString(7, donation.itemDescription().getOrNull());
                ps.setString(8, donation.status());
                ps.setTimestamp(9, Timestamp.valueOf(donation.donationDate()));
                ps.setTimestamp(10, Timestamp.valueOf(donation.createdDate()));
                
                int affected = ps.executeUpdate();
                if (affected > 0) {
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            Integer newId = keys.getInt(1);
                            logger.info("Donation created with ID: {}", newId);
                            return withId(donation, newId);
                        }
                    }
                }
                return donation;
            }
        });
    }
    
    public Try<Option<Donation>> findById(Integer id) {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblDonations WHERE DonationID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Option.of(mapToDonation(rs)) : Option.<Donation>none();
                }
            }
        });
    }
    
    public Try<List<Donation>> findAll() {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblDonations ORDER BY DonationDate DESC";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                List<Donation> donations = List.empty();
                while (rs.next()) {
                    donations = donations.append(mapToDonation(rs));
                }
                logger.debug("Found {} donations", donations.size());
                return donations;
            }
        });
    }
    
    public Try<List<Donation>> findByDonor(Integer donorId) {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblDonations WHERE DonorID = ? ORDER BY DonationDate DESC";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, donorId);
                try (ResultSet rs = ps.executeQuery()) {
                    List<Donation> donations = List.empty();
                    while (rs.next()) {
                        donations = donations.append(mapToDonation(rs));
                    }
                    return donations;
                }
            }
        });
    }
    
    public Try<List<Donation>> findByOrphanage(Integer orphanageId) {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblDonations WHERE OrphanageID = ? ORDER BY DonationDate DESC";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, orphanageId);
                try (ResultSet rs = ps.executeQuery()) {
                    List<Donation> donations = List.empty();
                    while (rs.next()) {
                        donations = donations.append(mapToDonation(rs));
                    }
                    return donations;
                }
            }
        });
    }
    
    public Try<Double> getTotalDonations() {
        return Try.of(() -> {
            String sql = "SELECT COALESCE(SUM(Amount), 0) as Total FROM TblDonations " +
                        "WHERE Status = 'Completed' AND DonationType = 'Money'";
            try (Connection conn = DatabaseManager.getConnection().get();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    return rs.getDouble("Total");
                }
                return 0.0;
            }
        });
    }
    
    public Try<Integer> getMonthlyDonationCount() {
        return Try.of(() -> {
            String sql = "SELECT COUNT(*) as Count FROM TblDonations WHERE " +
                        "strftime('%Y-%m', DonationDate) = strftime('%Y-%m', 'now') " +
                        "AND Status = 'Completed'";
            try (Connection conn = DatabaseManager.getConnection().get();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    return rs.getInt("Count");
                }
                return 0;
            }
        });
    }

    public Try<DonationStatistics> getStatistics(Integer orphanageId) {
        return Try.of(() -> {
            String sql = "SELECT " +
                         "COALESCE(SUM(CASE WHEN DonationType = 'Money' THEN Amount ELSE 0 END), 0) as totalAmount, " +
                         "COUNT(*) as totalCount " +
                         "FROM TblDonations WHERE OrphanageID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, orphanageId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        double totalAmount = rs.getDouble("totalAmount");
                        int totalCount = rs.getInt("totalCount");
                        // monthlyAverage can be calculated in the service layer if needed
                        return new DonationStatistics(totalAmount, totalCount, 0);
                    }
                    return new DonationStatistics(0, 0, 0);
                }
            }
        });
    }
    
    public Try<Void> update(Donation donation) {
        return Try.run(() -> {
            String sql = "UPDATE TblDonations SET DonorID = ?, OrphanageID = ?, " +
                        "DonationType = ?, Amount = ?, Status = ?, ModifiedDate = ? " +
                        "WHERE DonationID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, donation.donorId());
                ps.setInt(2, donation.orphanageId());
                ps.setString(3, donation.donationType());
                ps.setObject(4, donation.amount().getOrNull());
                ps.setString(5, donation.status());
                ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(7, donation.donationId());
                ps.executeUpdate();
            }
        });
    }

    public Try<Void> markCompleted(Integer donationId, String transactionRef) {
        return Try.run(() -> {
            String sql = "UPDATE TblDonations SET Status = 'Completed', TransactionReference = ?, ModifiedDate = ? WHERE DonationID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, transactionRef);
                ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(3, donationId);
                ps.executeUpdate();
            }
        });
    }
    
    /**
     * Cancels a recurring donation by setting RecurringDonation to false
     */
    public Try<Void> cancelRecurring(Integer donationId) {
        return Try.run(() -> {
            String sql = "UPDATE TblDonations SET RecurringDonation = 0, " +
                        "NextRecurrenceDate = NULL, ModifiedDate = ? WHERE DonationID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(2, donationId);
                int affected = ps.executeUpdate();
                if (affected > 0) {
                    logger.info("Cancelled recurring donation: {}", donationId);
                } else {
                    logger.warn("No donation found to cancel: {}", donationId);
                }
            }
        });
    }
    
    /**
     * Updates the next recurrence date for a recurring donation
     */
    public Try<Void> updateNextRecurrence(Integer donationId, LocalDateTime nextDate) {
        return Try.run(() -> {
            String sql = "UPDATE TblDonations SET NextRecurrenceDate = ?, ModifiedDate = ? WHERE DonationID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(nextDate));
                ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(3, donationId);
                int affected = ps.executeUpdate();
                if (affected > 0) {
                    logger.info("Updated next recurrence for donation {}: {}", donationId, nextDate);
                } else {
                    logger.warn("No donation found to update recurrence: {}", donationId);
                }
            }
        });
    }
    
    public Try<Void> delete(Integer id) {
        return Try.run(() -> {
            String sql = "DELETE FROM TblDonations WHERE DonationID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        });
    }
    
    public static class DonationStatistics {
        public final double totalAmount;
        public final int totalCount;
        public final double monthlyAverage;
        
        public DonationStatistics(double totalAmount, int totalCount, double monthlyAverage) {
            this.totalAmount = totalAmount;
            this.totalCount = totalCount;
            this.monthlyAverage = monthlyAverage;
        }
    }
    
    private Donation mapToDonation(ResultSet rs) throws SQLException {
        return new Donation(
            rs.getObject("DonationID", Integer.class),
            rs.getObject("DonorID", Integer.class),
            rs.getObject("OrphanageID", Integer.class),
            Option.of(rs.getObject("RequestID", Integer.class)),
            rs.getString("DonationType"),
            Option.of(rs.getObject("Amount", Double.class)),
            Option.of(rs.getString("Currency")).getOrElse("ZAR"),
            Option.of(rs.getString("ItemDescription")),
            Option.of(rs.getObject("Quantity", Double.class)),
            Option.of(rs.getString("Unit")),
            Option.of(rs.getObject("EstimatedValue", Double.class)),
            getLocalDateTime(rs, "DonationDate"),
            Option.of(rs.getTimestamp("ScheduledDate"))
                .map(Timestamp::toLocalDateTime),
            Option.of(rs.getString("Status")).getOrElse("Pending"),
            Option.of(rs.getString("PaymentMethod")),
            Option.of(rs.getString("TransactionReference")),
            rs.getBoolean("TaxDeductible"),
            rs.getBoolean("AnonymousDonation"),
            rs.getBoolean("RecurringDonation"),
            Option.of(rs.getString("RecurrenceInterval")),
            Option.of(rs.getTimestamp("NextRecurrenceDate"))
                .map(Timestamp::toLocalDateTime),
            Option.of(rs.getString("DonorMessage")),
            rs.getBoolean("ThankYouSent"),
            Option.of(rs.getTimestamp("ThankYouDate"))
                .map(Timestamp::toLocalDateTime),
            Option.of(rs.getString("ReceiptNumber")),
            rs.getBoolean("ReceiptSent"),
            Option.of(rs.getString("Notes")),
            getLocalDateTime(rs, "CreatedDate"),
            Option.of(rs.getTimestamp("ModifiedDate"))
                .map(Timestamp::toLocalDateTime),
            Option.of(rs.getObject("ModifiedBy", Integer.class))
        );
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
    }
    
    private Donation withId(Donation donation, Integer id) {
        return new Donation(
            id, donation.donorId(), donation.orphanageId(), donation.requestId(),
            donation.donationType(), donation.amount(), donation.currency(),
            donation.itemDescription(), donation.quantity(), donation.unit(),
            donation.estimatedValue(), donation.donationDate(), donation.scheduledDate(),
            donation.status(), donation.paymentMethod(), donation.transactionReference(),
            donation.taxDeductible(), donation.anonymousDonation(), donation.recurringDonation(),
            donation.recurrenceInterval(), donation.nextRecurrenceDate(), donation.donorMessage(),
            donation.thankYouSent(), donation.thankYouDate(), donation.receiptNumber(),
            donation.receiptSent(), donation.notes(), donation.createdDate(),
            donation.modifiedDate(), donation.modifiedBy()
        );
    }
}