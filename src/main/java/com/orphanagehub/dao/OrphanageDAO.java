package com.orphanagehub.dao;

import com.orphanagehub.model.Orphanage;
import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class OrphanageDAO {
    
    public Try<Orphanage> save(Orphanage orphanage) {
        return Try.of(() -> {
            String sql = "INSERT INTO TblOrphanages (OrphanageName, RegistrationNumber, TaxNumber, " +
                        "Address, City, Province, PostalCode, ContactPerson, ContactEmail, ContactPhone, " +
                        "AlternatePhone, Website, Description, Mission, Vision, EstablishedDate, " +
                        "Capacity, CurrentOccupancy, AgeGroupMin, AgeGroupMax, AcceptsDonations, " +
                        "AcceptsVolunteers, BankName, BankAccountNumber, BankBranchCode, " +
                        "UserID, VerificationStatus, DateRegistered, Status) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                
                ps.setString(1, orphanage.orphanageName().getOrElse(""));
                ps.setString(2, orphanage.registrationNumber().getOrNull());
                ps.setString(3, orphanage.taxNumber().getOrNull());
                ps.setString(4, orphanage.address());
                ps.setString(5, orphanage.city());
                ps.setString(6, orphanage.province());
                ps.setString(7, orphanage.postalCode().getOrNull());
                ps.setString(8, orphanage.contactPerson());
                ps.setString(9, orphanage.contactEmail());
                ps.setString(10, orphanage.contactPhone());
                ps.setString(11, orphanage.alternatePhone().getOrNull());
                ps.setString(12, orphanage.website().getOrNull());
                ps.setString(13, orphanage.description().getOrNull());
                ps.setString(14, orphanage.mission().getOrNull());
                ps.setString(15, orphanage.vision().getOrNull());
                
                if (orphanage.establishedDate().isDefined()) {
                    ps.setDate(16, Date.valueOf(orphanage.establishedDate().get()));
                } else {
                    ps.setNull(16, Types.DATE);
                }
                
                // Use setObject for nullable integers
                ps.setObject(17, orphanage.capacity().getOrNull(), Types.INTEGER);
                ps.setObject(18, orphanage.currentOccupancy().getOrNull(), Types.INTEGER);
                ps.setObject(19, orphanage.ageGroupMin().getOrNull(), Types.INTEGER);
                ps.setObject(20, orphanage.ageGroupMax().getOrNull(), Types.INTEGER);
                
                ps.setBoolean(21, orphanage.acceptsDonations());
                ps.setBoolean(22, orphanage.acceptsVolunteers());
                ps.setString(23, orphanage.bankName().getOrNull());
                ps.setString(24, orphanage.bankAccountNumber().getOrNull());
                ps.setString(25, orphanage.bankBranchCode().getOrNull());
                ps.setInt(26, orphanage.userId());
                ps.setString(27, orphanage.verificationStatus());
                ps.setTimestamp(28, Timestamp.valueOf(orphanage.dateRegistered()));
                ps.setString(29, orphanage.status());
                
                ps.executeUpdate();
                
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return findById(keys.getInt(1)).get().get();
                    }
                }
                return orphanage;
            }
        });
    }
    
    // Alias for save method
    public Try<Orphanage> create(Orphanage orphanage) {
        return save(orphanage);
    }
    
    public Try<Option<Orphanage>> findById(Integer id) {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblOrphanages WHERE OrphanageID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Option.of(mapRowToOrphanage(rs)) : Option.<Orphanage>none();
                }
            }
        });
    }
    
    public Try<Option<Orphanage>> findByUserId(Integer userId) {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblOrphanages WHERE UserID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Option.of(mapRowToOrphanage(rs)) : Option.<Orphanage>none();
                }
            }
        });
    }
    
    public Try<List<Orphanage>> findAll() {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblOrphanages ORDER BY OrphanageName";
            try (Connection conn = DatabaseManager.getConnection().get();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                List<Orphanage> orphanages = List.empty();
                while (rs.next()) {
                    orphanages = orphanages.append(mapRowToOrphanage(rs));
                }
                return orphanages;
            }
        });
    }
    
    public Try<List<Orphanage>> findByVerificationStatus(String status) {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblOrphanages WHERE VerificationStatus = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, status);
                try (ResultSet rs = ps.executeQuery()) {
                    List<Orphanage> orphanages = List.empty();
                    while (rs.next()) {
                        orphanages = orphanages.append(mapRowToOrphanage(rs));
                    }
                    return orphanages;
                }
            }
        });
    }
    
    public Try<List<Orphanage>> findAllVerified() {
        return findByVerificationStatus("Verified");
    }

    public Try<List<String>> getAllProvinces() {
        return Try.of(() -> {
            String sql = "SELECT DISTINCT Province FROM TblOrphanages ORDER BY Province";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                List<String> provinces = List.empty();
                while (rs.next()) {
                    provinces = provinces.append(rs.getString("Province"));
                }
                return provinces;
            }
        });
    }
    
    public Try<Void> update(Orphanage orphanage) {
        return Try.run(() -> {
            String sql = "UPDATE TblOrphanages SET OrphanageName = ?, RegistrationNumber = ?, TaxNumber = ?, " +
                        "Address = ?, City = ?, Province = ?, PostalCode = ?, ContactPerson = ?, " +
                        "ContactEmail = ?, ContactPhone = ?, AlternatePhone = ?, Website = ?, " +
                        "Description = ?, Mission = ?, Vision = ?, EstablishedDate = ?, " +
                        "Capacity = ?, CurrentOccupancy = ?, AgeGroupMin = ?, AgeGroupMax = ?, " +
                        "AcceptsDonations = ?, AcceptsVolunteers = ?, BankName = ?, " +
                        "BankAccountNumber = ?, BankBranchCode = ?, VerificationStatus = ?, " +
                        "ModifiedDate = ? WHERE OrphanageID = ?";
            
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                ps.setString(1, orphanage.orphanageName().getOrElse(""));
                ps.setString(2, orphanage.registrationNumber().getOrNull());
                ps.setString(3, orphanage.taxNumber().getOrNull());
                ps.setString(4, orphanage.address());
                ps.setString(5, orphanage.city());
                ps.setString(6, orphanage.province());
                ps.setString(7, orphanage.postalCode().getOrNull());
                ps.setString(8, orphanage.contactPerson());
                ps.setString(9, orphanage.contactEmail());
                ps.setString(10, orphanage.contactPhone());
                ps.setString(11, orphanage.alternatePhone().getOrNull());
                ps.setString(12, orphanage.website().getOrNull());
                ps.setString(13, orphanage.description().getOrNull());
                ps.setString(14, orphanage.mission().getOrNull());
                ps.setString(15, orphanage.vision().getOrNull());
                
                if (orphanage.establishedDate().isDefined()) {
                    ps.setDate(16, Date.valueOf(orphanage.establishedDate().get()));
                } else {
                    ps.setNull(16, Types.DATE);
                }
                
                ps.setObject(17, orphanage.capacity().getOrNull(), Types.INTEGER);
                ps.setObject(18, orphanage.currentOccupancy().getOrNull(), Types.INTEGER);
                ps.setObject(19, orphanage.ageGroupMin().getOrNull(), Types.INTEGER);
                ps.setObject(20, orphanage.ageGroupMax().getOrNull(), Types.INTEGER);
                
                ps.setBoolean(21, orphanage.acceptsDonations());
                ps.setBoolean(22, orphanage.acceptsVolunteers());
                ps.setString(23, orphanage.bankName().getOrNull());
                ps.setString(24, orphanage.bankAccountNumber().getOrNull());
                ps.setString(25, orphanage.bankBranchCode().getOrNull());
                ps.setString(26, orphanage.verificationStatus());
                ps.setTimestamp(27, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(28, orphanage.orphanageId());
                
                ps.executeUpdate();
            }
        });
    }
    
    public Try<Void> verifyOrphanage(Integer orphanageId, Integer verifiedBy, String notes) {
        return Try.run(() -> {
            String sql = "UPDATE TblOrphanages SET VerificationStatus = 'Verified', " +
                        "VerificationDate = ?, VerifiedBy = ?, VerificationNotes = ? " +
                        "WHERE OrphanageID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(2, verifiedBy);
                ps.setString(3, notes);
                ps.setInt(4, orphanageId);
                ps.executeUpdate();
            }
        });
    }
    
    public Try<Void> updateStatus(Integer orphanageId, String status) {
        return Try.run(() -> {
            String sql = "UPDATE TblOrphanages SET Status = ?, ModifiedDate = ? WHERE OrphanageID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, status);
                ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(3, orphanageId);
                ps.executeUpdate();
            }
        });
    }
    
    public Try<Void> delete(Integer orphanageId) {
        return updateStatus(orphanageId, "Inactive");
    }
    
    /**
     * Safe method to get Integer from ResultSet, handling SQLite's flexible typing
     */
    private Integer getIntegerSafe(ResultSet rs, String columnName) throws SQLException {
        Object value = rs.getObject(columnName);
        if (value == null) return null;
        
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Long) {
            return ((Long) value).intValue();
        } else if (value instanceof Double) {
            return ((Double) value).intValue();
        } else if (value instanceof Float) {
            return ((Float) value).intValue();
        } else if (value instanceof String) {
            try {
                // Try to parse as double first (handles decimals), then convert to int
                return Double.valueOf((String) value).intValue();
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * Safe method to get LocalDate from ResultSet
     */
    private LocalDate getLocalDateSafe(ResultSet rs, String columnName) throws SQLException {
        try {
            Date date = rs.getDate(columnName);
            return date != null ? date.toLocalDate() : null;
        } catch (SQLException e) {
            // Try as timestamp
            Timestamp ts = rs.getTimestamp(columnName);
            return ts != null ? ts.toLocalDateTime().toLocalDate() : null;
        }
    }
    
    /**
     * Safe method to get LocalDateTime from ResultSet
     */
    private LocalDateTime getLocalDateTimeSafe(ResultSet rs, String columnName) throws SQLException {
        Timestamp ts = rs.getTimestamp(columnName);
        return ts != null ? ts.toLocalDateTime() : null;
    }
    
    /**
     * Safe method to get Double from ResultSet
     */
    private Double getDoubleSafe(ResultSet rs, String columnName) throws SQLException {
        Object value = rs.getObject(columnName);
        if (value == null) return null;
        
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Float) {
            return ((Float) value).doubleValue();
        } else if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        } else if (value instanceof Long) {
            return ((Long) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.valueOf((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    private Orphanage mapRowToOrphanage(ResultSet rs) throws SQLException {
        return new Orphanage(
            getIntegerSafe(rs, "OrphanageID"),
            Option.of(rs.getString("OrphanageName")),
            Option.of(rs.getString("RegistrationNumber")),
            Option.of(rs.getString("TaxNumber")),
            rs.getString("Address"),
            rs.getString("City"),
            rs.getString("Province"),
            Option.of(rs.getString("PostalCode")),
            rs.getString("ContactPerson"),
            rs.getString("ContactEmail"),
            rs.getString("ContactPhone"),
            Option.of(rs.getString("AlternatePhone")),
            Option.of(rs.getString("Website")),
            Option.of(rs.getString("Description")),
            Option.of(rs.getString("Mission")),
            Option.of(rs.getString("Vision")),
            Option.of(getLocalDateSafe(rs, "EstablishedDate")),
            Option.of(getIntegerSafe(rs, "Capacity")),
            Option.of(getIntegerSafe(rs, "CurrentOccupancy")),
            Option.of(getIntegerSafe(rs, "AgeGroupMin")),
            Option.of(getIntegerSafe(rs, "AgeGroupMax")),
            rs.getBoolean("AcceptsDonations"),
            rs.getBoolean("AcceptsVolunteers"),
            Option.of(rs.getString("BankName")),
            Option.of(rs.getString("BankAccountNumber")),
            Option.of(rs.getString("BankBranchCode")),
            getLocalDateTimeSafe(rs, "DateRegistered") != null ? 
                getLocalDateTimeSafe(rs, "DateRegistered") : LocalDateTime.now(),
            rs.getString("VerificationStatus"),
            Option.of(getLocalDateTimeSafe(rs, "VerificationDate")),
            Option.of(getIntegerSafe(rs, "VerifiedBy")),
            Option.of(rs.getString("VerificationNotes")),
            getIntegerSafe(rs, "UserID") != null ? getIntegerSafe(rs, "UserID") : 0,
            rs.getString("Status") != null ? rs.getString("Status") : "Active",
            Option.of(rs.getString("Logo")),
            Option.of(rs.getString("CoverImage")),
            Option.of(getDoubleSafe(rs, "Latitude")),
            Option.of(getDoubleSafe(rs, "Longitude")),
            Option.of(getLocalDateTimeSafe(rs, "ModifiedDate")),
            Option.of(getIntegerSafe(rs, "ModifiedBy"))
        );
    }
}