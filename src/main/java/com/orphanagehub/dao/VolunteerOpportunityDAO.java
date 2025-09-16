package com.orphanagehub.dao;

import com.orphanagehub.model.VolunteerOpportunity;
import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class VolunteerOpportunityDAO {
    
    public Try<Void> create(VolunteerOpportunity opportunity) {
        return Try.run(() -> {
            String sql = "INSERT INTO TblVolunteerOpportunities (OrphanageID, Title, Description, " +
                        "Category, SkillsRequired, Status, CreatedDate, CreatedBy) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, opportunity.orphanageId());
                ps.setString(2, opportunity.title());
                ps.setString(3, opportunity.description());
                ps.setString(4, opportunity.category());
                ps.setString(5, opportunity.skillsRequired().getOrNull());
                ps.setString(6, opportunity.status());
                ps.setTimestamp(7, Timestamp.valueOf(opportunity.createdDate()));
                ps.setInt(8, opportunity.createdBy());
                ps.executeUpdate();
            }
        });
    }
    
    public Try<Option<VolunteerOpportunity>> findById(Integer id) {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblVolunteerOpportunities WHERE OpportunityID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Option.of(mapToVolunteerOpportunity(rs)) : Option.none();
                }
            }
        });
    }
    
    public Try<List<VolunteerOpportunity>> findOpenOpportunities() {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblVolunteerOpportunities WHERE Status = 'Open' " +
                        "ORDER BY CreatedDate DESC";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                List<VolunteerOpportunity> opportunities = List.empty();
                while (rs.next()) {
                    opportunities = opportunities.append(mapToVolunteerOpportunity(rs));
                }
                return opportunities;
            }
        });
    }
    
    public Try<List<VolunteerOpportunity>> findByOrphanageId(Integer orphanageId) {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblVolunteerOpportunities WHERE OrphanageID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, orphanageId);
                try (ResultSet rs = ps.executeQuery()) {
                    List<VolunteerOpportunity> opportunities = List.empty();
                    while (rs.next()) {
                        opportunities = opportunities.append(mapToVolunteerOpportunity(rs));
                    }
                    return opportunities;
                }
            }
        });
    }
    
    public Try<Void> updateStatus(Integer opportunityId, String status) {
        return Try.run(() -> {
            String sql = "UPDATE TblVolunteerOpportunities SET Status = ? WHERE OpportunityID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, status);
                ps.setInt(2, opportunityId);
                ps.executeUpdate();
            }
        });
    }
    
    public Try<Void> decrementVolunteers(Integer opportunityId) {
        return Try.run(() -> {
            String sql = "UPDATE TblVolunteerOpportunities SET CurrentVolunteers = " +
                        "CurrentVolunteers - 1 WHERE OpportunityID = ? AND CurrentVolunteers > 0";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, opportunityId);
                ps.executeUpdate();
            }
        });
    }
    
    public Try<Void> update(VolunteerOpportunity opportunity) {
        return Try.run(() -> {
            String sql = "UPDATE TblVolunteerOpportunities SET Title = ?, Description = ?, " +
                        "Category = ?, SkillsRequired = ?, Status = ? WHERE OpportunityID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, opportunity.title());
                ps.setString(2, opportunity.description());
                ps.setString(3, opportunity.category());
                ps.setString(4, opportunity.skillsRequired().getOrNull());
                ps.setString(5, opportunity.status());
                ps.setInt(6, opportunity.opportunityId());
                ps.executeUpdate();
            }
        });
    }
    
    // Add executeQuery method for flexible queries
    public <T> Try<List<T>> executeQuery(String sql, List<Object> params, java.util.function.Function<ResultSet, T> rowMapper) {
        return Try.of(() -> {
            List<T> results = List.empty();
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        try {
                            results = results.append(rowMapper.apply(rs));
                        } catch (Exception e) {
                            // Log error but continue processing other rows
                            System.err.println("Error mapping row: " + e.getMessage());
                        }
                    }
                }
            }
            return results;
        });
    }
    
    private VolunteerOpportunity mapToVolunteerOpportunity(ResultSet rs) throws SQLException {
        return new VolunteerOpportunity(
            rs.getInt("OpportunityID"),
            rs.getInt("OrphanageID"),
            rs.getString("Title"),
            rs.getString("Description"),
            getStringOrNull(rs, "Category"),  // Handle missing columns
            Option.of(getStringOrNull(rs, "RequiredSkills")),
            Option.none(), // SkillLevel not in table
            Option.of(getStringOrNull(rs, "TimeCommitment")),
            Option.none(), // HoursPerWeek not in table
            Option.none(), // Duration not in table
            Option.none(), // StartDate not in table
            Option.none(), // EndDate not in table
            Option.none(), // RecurringSchedule not in table
            Option.none(), // MinAge not in table
            Option.none(), // MaxAge not in table
            Option.of(getIntOrNull(rs, "Capacity")),
            getIntOrDefault(rs, "CurrentVolunteers", 0),
            false, // BackgroundCheckRequired not in table
            false, // TrainingProvided not in table
            Option.none(), // TrainingDetails not in table
            false, // TransportProvided not in table
            false, // MealsProvided not in table
            getStringOrDefault(rs, "Status", "Open"),
            "Normal", // UrgencyLevel not in table
            getTimestampOrNow(rs, "PostedDate"),
            getIntOrDefault(rs, "PostedBy", 0),
            Option.none(), // ModifiedDate
            Option.none(), // ModifiedBy
            Option.none(), // PublishedDate
            Option.none(), // ClosedDate
            Option.of(getStringOrNull(rs, "Location"))
        );
    }
    
    // Helper methods to handle missing columns gracefully
    private String getStringOrNull(ResultSet rs, String columnName) {
        try {
            return rs.getString(columnName);
        } catch (SQLException e) {
            return null;
        }
    }
    
    private String getStringOrDefault(ResultSet rs, String columnName, String defaultValue) {
        try {
            String value = rs.getString(columnName);
            return value != null ? value : defaultValue;
        } catch (SQLException e) {
            return defaultValue;
        }
    }
    
    private Integer getIntOrNull(ResultSet rs, String columnName) {
        try {
            int value = rs.getInt(columnName);
            return rs.wasNull() ? null : value;
        } catch (SQLException e) {
            return null;
        }
    }
    
    private int getIntOrDefault(ResultSet rs, String columnName, int defaultValue) {
        try {
            int value = rs.getInt(columnName);
            return rs.wasNull() ? defaultValue : value;
        } catch (SQLException e) {
            return defaultValue;
        }
    }
    
    private LocalDateTime getTimestampOrNow(ResultSet rs, String columnName) {
        try {
            Timestamp ts = rs.getTimestamp(columnName);
            return ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
        } catch (SQLException e) {
            return LocalDateTime.now();
        }
    }
}