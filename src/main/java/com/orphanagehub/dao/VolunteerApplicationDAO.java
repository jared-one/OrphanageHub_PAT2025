package com.orphanagehub.dao;

import com.orphanagehub.model.VolunteerApplication;
import io.vavr.control.Try;
import io.vavr.control.Option;
import io.vavr.collection.List;
import java.sql.*;
import java.time.LocalDateTime;

public class VolunteerApplicationDAO {
    
    public Try<VolunteerApplication> create(VolunteerApplication application) {
        return Try.of(() -> {
            String sql = "INSERT INTO TblVolunteerApplications (OpportunityID, VolunteerID, " +
                        "ApplicationDate, Status, Motivation, Experience, Availability, " +
                        "CreatedDate) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, application.opportunityId());
                ps.setInt(2, application.volunteerId());
                ps.setTimestamp(3, Timestamp.valueOf(application.applicationDate()));
                ps.setString(4, application.status());
                ps.setString(5, application.motivation().getOrNull());
                ps.setString(6, application.experience().getOrNull());
                ps.setString(7, application.availability().getOrNull());
                ps.setTimestamp(8, Timestamp.valueOf(application.createdDate()));
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return findById(keys.getInt(1)).get().get();
                    }
                }
                return application;
            }
        });
    }
    
    public Try<Option<VolunteerApplication>> findById(Integer id) {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblVolunteerApplications WHERE ApplicationID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Option.of(mapToVolunteerApplication(rs)) : Option.none();
                }
            }
        });
    }
    
    public Try<List<VolunteerApplication>> findByOpportunity(Integer opportunityId) {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblVolunteerApplications WHERE OpportunityID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, opportunityId);
                try (ResultSet rs = ps.executeQuery()) {
                    List<VolunteerApplication> apps = List.empty();
                    while (rs.next()) {
                        apps = apps.append(mapToVolunteerApplication(rs));
                    }
                    return apps;
                }
            }
        });
    }
    
    public Try<Boolean> hasApplied(Integer volunteerId, Integer opportunityId) {
        return Try.of(() -> {
            String sql = "SELECT COUNT(*) > 0 FROM TblVolunteerApplications " +
                        "WHERE VolunteerID = ? AND OpportunityID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, volunteerId);
                ps.setInt(2, opportunityId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() && rs.getBoolean(1);
                }
            }
        });
    }
    
    public Try<List<VolunteerApplication>> findByVolunteerId(Integer volunteerId) {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblVolunteerApplications WHERE VolunteerID = ? " +
                        "ORDER BY ApplicationDate DESC";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, volunteerId);
                try (ResultSet rs = ps.executeQuery()) {
                    List<VolunteerApplication> apps = List.empty();
                    while (rs.next()) {
                        apps = apps.append(mapToVolunteerApplication(rs));
                    }
                    return apps;
                }
            }
        });
    }
    
    public Try<Void> updateStatus(Integer applicationId, String status, Integer updatedBy) {
        return Try.run(() -> {
            String sql = "UPDATE TblVolunteerApplications SET Status = ?, ModifiedBy = ?, " +
                        "ModifiedDate = CURRENT_TIMESTAMP WHERE ApplicationID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, status);
                ps.setInt(2, updatedBy);
                ps.setInt(3, applicationId);
                ps.executeUpdate();
            }
        });
    }
    
    public Try<Integer> getPendingApplicationCount() {
        return Try.of(() -> {
            String sql = "SELECT COUNT(*) FROM TblVolunteerApplications " +
                        "WHERE Status IN ('Pending', 'Reviewing')";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        });
    }
    private VolunteerApplication mapToVolunteerApplication(ResultSet rs) throws SQLException {
    return new VolunteerApplication(
        rs.getInt("ApplicationID"),
        rs.getInt("OpportunityID"),  
        rs.getInt("VolunteerID"),
        rs.getTimestamp("ApplicationDate").toLocalDateTime(),
        rs.getString("Status"),
        Option.of(rs.getString("Motivation")),
        Option.of(rs.getString("Experience")),
        Option.of(rs.getString("Availability")),
        Option.of(rs.getTimestamp("InterviewDate"))
            .map(Timestamp::toLocalDateTime),
        Option.of(rs.getString("InterviewNotes")),
        Option.of(rs.getTimestamp("DecisionDate"))
            .map(Timestamp::toLocalDateTime),
        Option.of(rs.getObject("DecidedBy", Integer.class)),
        Option.of(rs.getString("RejectionReason")),
        Option.of(rs.getTimestamp("StartDate"))
            .map(Timestamp::toLocalDateTime),
        Option.of(rs.getTimestamp("EndDate"))
            .map(Timestamp::toLocalDateTime),
        Option.of(rs.getString("CompletionNotes")),
        Option.of(rs.getObject("HoursCompleted", Integer.class)),
        Option.of(rs.getString("PerformanceRating")),
        rs.getTimestamp("CreatedDate").toLocalDateTime(),
        Option.of(rs.getTimestamp("ModifiedDate"))
            .map(Timestamp::toLocalDateTime),
        Option.of(rs.getObject("ModifiedBy", Integer.class))
    );
}
}