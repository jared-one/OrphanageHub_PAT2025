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
    
    public Try<Void> create(Donation donation) {
        return Try.run(() -> {
            String sql = "INSERT INTO TblDonations (DonationID, DonorID, OrphanageID, ItemCategory, " +
                       "ItemDescription, Amount, Status, DateDonated) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                String donationId = donation.donationId() != null ? donation.donationId() :
                    "DON" + UUID.randomUUID().toString().substring(0, 7).toUpperCase();
                
                ps.setString(1, donationId);
                ps.setString(2, donation.donorId());
                ps.setString(3, donation.orphanageId());
                ps.setString(4, donation.itemCategory());
                ps.setString(5, donation.itemDescription().getOrNull());
                
                donation.amount()
                    .map(amt -> Try.run(() -> ps.setInt(6, amt)))
                    .getOrElse(Try.run(() -> ps.setNull(6, Types.INTEGER)));
                
                ps.setString(7, donation.status());
                ps.setTimestamp(8, donation.dateDonated());
                
                int affected = ps.executeUpdate();
                if (affected > 0) {
                    logger.info("Donation created with ID: {}", donationId);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    public Try<Option<Donation>> findById(String id) {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblDonations WHERE DonationID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Option.of(mapToDonation(rs)) : Option.<Donation>none();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    public Try<List<Donation>> findAll() {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblDonations ORDER BY DateDonated DESC";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                List<Donation> donations = List.empty();
                while (rs.next()) {
                    donations = donations.append(mapToDonation(rs));
                }
                logger.debug("Found {} donations", donations.size());
                return donations;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    public Try<Void> update(Donation donation) {
        return Try.run(() -> {
            String sql = "UPDATE TblDonations SET DonorID = ?, OrphanageID = ?, ItemCategory = ?, ItemDescription = ?, Amount = ?, Status = ?, DateDonated = ? WHERE DonationID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, donation.donorId());
                ps.setString(2, donation.orphanageId());
                ps.setString(3, donation.itemCategory());
                ps.setString(4, donation.itemDescription().getOrNull());
                if (donation.amount().isDefined()) {
                    ps.setInt(5, donation.amount().get());
                } else {
                    ps.setNull(5, Types.INTEGER);
                }
                ps.setString(6, donation.status());
                ps.setTimestamp(7, donation.dateDonated());
                ps.setString(8, donation.donationId());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    public Try<Void> delete(String id) {
        return Try.run(() -> {
            String sql = "DELETE FROM TblDonations WHERE DonationID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    private Donation mapToDonation(ResultSet rs) throws SQLException {
        Object amountObj = rs.getObject("Amount");
        Option<Integer> amount = amountObj == null ? Option.none() : Option.of(rs.getInt("Amount"));
        return new Donation(
            rs.getString("DonationID"),
            rs.getString("DonorID"),
            rs.getString("OrphanageID"),
            rs.getString("ItemCategory"),
            Option.of(rs.getString("ItemDescription")),
            amount,
            rs.getString("Status"),
            rs.getTimestamp("DateDonated")
        );
    }
}