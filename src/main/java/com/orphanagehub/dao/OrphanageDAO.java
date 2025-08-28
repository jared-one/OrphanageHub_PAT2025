/* Copyright (C) 2025 Jared Wisdom - All Rights Reserved */
package com.orphanagehub.dao;

import com.orphanagehub.model.Orphanage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrphanageDAO {
    private static final Logger logger = LoggerFactory.getLogger(OrphanageDAO.class);

    public Orphanage save(Orphanage orphanage) throws SQLException {
        String sql = "INSERT INTO Orphanages (OrphanageID, Name, Address, ContactEmail, ContactPhone, " +
                     "Description, Capacity, CurrentOccupancy, DateEstablished, Status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        if (orphanage.getOrphanageId() == null || orphanage.getOrphanageId().isEmpty()) {
            orphanage.setOrphanageId(UUID.randomUUID().toString());
        }

        if (orphanage.getDateEstablished() == null) {
            orphanage.setDateEstablished(LocalDateTime.now());
        }

        if (orphanage.getStatus() == null || orphanage.getStatus().isEmpty()) {
            orphanage.setStatus("ACTIVE");
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, orphanage.getOrphanageId());
            stmt.setString(2, orphanage.getName());
            stmt.setString(3, orphanage.getAddress());
            stmt.setString(4, orphanage.getContactEmail());
            stmt.setString(5, orphanage.getContactPhone());
            stmt.setString(6, orphanage.getDescription());
            stmt.setInt(7, orphanage.getCapacity());
            stmt.setInt(8, orphanage.getCurrentOccupancy());
            stmt.setTimestamp(9, Timestamp.valueOf(orphanage.getDateEstablished()));
            stmt.setString(10, orphanage.getStatus());

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                logger.info("Orphanage saved successfully: {}", orphanage.getName());
                return orphanage;
            } else {
                throw new SQLException("Failed to insert orphanage, no rows affected");
            }
        } catch (SQLException e) {
            logger.error("Error saving orphanage: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<Orphanage> findAll() throws SQLException {
        String sql = "SELECT * FROM Orphanages WHERE Status = 'ACTIVE' ORDER BY Name";
        List<Orphanage> orphanages = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Orphanage orphanage = mapResultSetToOrphanage(rs);
                orphanages.add(orphanage);
            }

            logger.debug("Found {} orphanages", orphanages.size());
        } catch (SQLException e) {
            logger.error("Error fetching orphanages: {}", e.getMessage(), e);
            throw e;
        }

        return orphanages;
    }

    public Orphanage findById(String orphanageId) throws SQLException {
        String sql = "SELECT * FROM Orphanages WHERE OrphanageID = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, orphanageId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Orphanage orphanage = mapResultSetToOrphanage(rs);
                    logger.debug("Orphanage found: {}", orphanage.getName());
                    return orphanage;
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding orphanage by ID: {}", e.getMessage(), e);
            throw e;
        }

        return null;
    }

    public Orphanage findByName(String name) throws SQLException {
        String sql = "SELECT * FROM Orphanages WHERE Name = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Orphanage orphanage = mapResultSetToOrphanage(rs);
                    logger.debug("Orphanage found by name: {}", orphanage.getName());
                    return orphanage;
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding orphanage by name: {}", e.getMessage(), e);
            throw e;
        }

        return null;
    }

    public Orphanage findByStaffUserId(String userId) throws SQLException {
        String sql = "SELECT o.* FROM Orphanages o INNER JOIN Users u ON o.OrphanageID = u.OrphanageID WHERE u.UserID = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Orphanage orphanage = mapResultSetToOrphanage(rs);
                    logger.debug("Orphanage found for staff user {}: {}", userId, orphanage.getName());
                    return orphanage;
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding orphanage by staff user ID: {}", e.getMessage(), e);
            throw e;
        }

        return null;
    }

    private Orphanage mapResultSetToOrphanage(ResultSet rs) throws SQLException {
        Orphanage orphanage = new Orphanage();
        orphanage.setOrphanageId(rs.getString("OrphanageID"));
        orphanage.setName(rs.getString("Name"));
        orphanage.setAddress(rs.getString("Address"));
        orphanage.setContactEmail(rs.getString("ContactEmail"));
        orphanage.setContactPhone(rs.getString("ContactPhone"));
        orphanage.setDescription(rs.getString("Description"));
        orphanage.setCapacity(rs.getInt("Capacity"));
        orphanage.setCurrentOccupancy(rs.getInt("CurrentOccupancy"));

        Timestamp dateEst = rs.getTimestamp("DateEstablished");
        if (dateEst != null) {
            orphanage.setDateEstablished(dateEst.toLocalDateTime());
        }

        orphanage.setStatus(rs.getString("Status"));

        return orphanage;
    }
}
