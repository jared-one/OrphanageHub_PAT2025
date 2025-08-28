package com.orphanagehub.dao;

import com.orphanagehub.model.Orphanage;
import com.orphanagehub.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrphanageDAO {
    private static final Logger logger = LoggerFactory.getLogger(OrphanageDAO.class);

    public void save(Orphanage orphanage) throws SQLException {
        String sql;
        if (orphanage.getOrphanageId() == null) {
            orphanage.setOrphanageId("ORPH" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
            orphanage.setVerificationStatus("PENDING");
            sql = "INSERT INTO TblOrphanages (OrphanageID, UserID, Name, Address, ContactPerson, ContactEmail, ContactPhone, VerificationStatus) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
            sql = "UPDATE TblOrphanages SET UserID = ?, Name = ?, Address = ?, ContactPerson = ?, ContactEmail = ?, ContactPhone = ?, VerificationStatus = ? " +
                  "WHERE OrphanageID = ?";
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, orphanage.getUserId());
            stmt.setString(2, orphanage.getName());
            stmt.setString(3, orphanage.getAddress());
            stmt.setString(4, orphanage.getContactPerson());
            stmt.setString(5, orphanage.getContactEmail());
            stmt.setString(6, orphanage.getContactPhone());
            stmt.setString(7, orphanage.getVerificationStatus());

            if (orphanage.getOrphanageId() != null) {
                stmt.setString(8, orphanage.getOrphanageId());
            } else {
                stmt.setString(1, orphanage.getOrphanageId());
                stmt.setString(2, orphanage.getUserId());
                stmt.setString(8, orphanage.getVerificationStatus());
            }

            stmt.executeUpdate();
            logger.info("Orphanage saved: {}", orphanage.getName());
        }
    }

    public List<Orphanage> findAll() throws SQLException {
        List<Orphanage> orphanages = new ArrayList<>();
        String sql = "SELECT * FROM TblOrphanages";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Orphanage orphanage = new Orphanage(
                    rs.getString("OrphanageID"),
                    rs.getString("UserID"),
                    rs.getString("Name"),
                    rs.getString("Address"),
                    rs.getString("ContactPerson"),
                    rs.getString("ContactEmail"),
                    rs.getString("ContactPhone"),
                    rs.getString("VerificationStatus")
                );
                orphanages.add(orphanage);
            }
        }
        return orphanages;
    }

    public Orphanage findByStaffUserId(String userId) throws SQLException {
        String sql = "SELECT * FROM TblOrphanages WHERE UserID = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Orphanage(
                        rs.getString("OrphanageID"),
                        rs.getString("UserID"),
                        rs.getString("Name"),
                        rs.getString("Address"),
                        rs.getString("ContactPerson"),
                        rs.getString("ContactEmail"),
                        rs.getString("ContactPhone"),
                        rs.getString("VerificationStatus")
                    );
                }
                return null;
            }
        }
    }

    // Add more methods as needed...
}