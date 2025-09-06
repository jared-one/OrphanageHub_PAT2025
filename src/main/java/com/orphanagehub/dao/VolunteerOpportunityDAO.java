package com.orphanagehub.dao;

import com.orphanagehub.model.VolunteerOpportunity;
import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.sql.*;
import java.util.UUID;

public class VolunteerOpportunityDAO {
    
    public Try<Void> create(VolunteerOpportunity opportunity) {
        return DatabaseManager.getConnection()
            .flatMap(conn -> Try.withResources(() -> conn)
                .of(connection -> {
                    String sql = "INSERT INTO TblVolunteerOpportunities (OpportunityID, OrphanageID, SkillRequired, Location, TimeCommitment, Status) VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = connection.prepareStatement(sql)) {
                        ps.setString(1, UUID.randomUUID().toString().substring(0, 10));
                        ps.setString(2, opportunity.orphanageId());
                        ps.setString(3, opportunity.skillRequired());
                        ps.setString(4, opportunity.location());
                        ps.setString(5, opportunity.timeCommitment().getOrNull());
                        ps.setString(6, opportunity.status());
                        ps.executeUpdate();
                    }
                    return null;
                }));
    }
    
    public Try<Option<VolunteerOpportunity>> findById(String id) {
        return DatabaseManager.getConnection()
            .flatMap(conn -> Try.withResources(() -> conn)
                .of(connection -> {
                    String sql = "SELECT * FROM TblVolunteerOpportunities WHERE OpportunityID = ?";
                    try (PreparedStatement ps = connection.prepareStatement(sql)) {
                        ps.setString(1, id);
                        ResultSet rs = ps.executeQuery();
                        return rs.next() ? Option.of(mapToVolunteerOpportunity(rs)) : Option.<VolunteerOpportunity>none();
                    }
                }));
    }
    
    public Try<List<VolunteerOpportunity>> findByOrphanageId(String orphanageId) {
        return DatabaseManager.getConnection()
            .flatMap(conn -> Try.withResources(() -> conn)
                .of(connection -> {
                    String sql = "SELECT * FROM TblVolunteerOpportunities WHERE OrphanageID = ?";
                    List<VolunteerOpportunity> opportunities = List.empty();
                    try (PreparedStatement ps = connection.prepareStatement(sql)) {
                        ps.setString(1, orphanageId);
                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
                            opportunities = opportunities.append(mapToVolunteerOpportunity(rs));
                        }
                    }
                    return opportunities;
                }));
    }
    
    public Try<List<VolunteerOpportunity>> findAll() {
        return DatabaseManager.getConnection()
            .flatMap(conn -> Try.withResources(() -> conn)
                .of(connection -> {
                    String sql = "SELECT * FROM TblVolunteerOpportunities";
                    List<VolunteerOpportunity> opportunities = List.empty();
                    try (PreparedStatement ps = connection.prepareStatement(sql)) {
                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
                            opportunities = opportunities.append(mapToVolunteerOpportunity(rs));
                        }
                    }
                    return opportunities;
                }));
    }
    
    public Try<Void> update(VolunteerOpportunity opportunity) {
        return Option.of(opportunity.opportunityId())
                .toTry(() -> new IllegalArgumentException("Opportunity ID required for update"))
                .flatMap(id -> DatabaseManager.getConnection()
                    .flatMap(conn -> Try.withResources(() -> conn)
                        .of(connection -> {
                            String sql = "UPDATE TblVolunteerOpportunities SET OrphanageID = ?, SkillRequired = ?, Location = ?, TimeCommitment = ?, Status = ? WHERE OpportunityID = ?";
                            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                                ps.setString(1, opportunity.orphanageId());
                                ps.setString(2, opportunity.skillRequired());
                                ps.setString(3, opportunity.location());
                                ps.setString(4, opportunity.timeCommitment().getOrNull());
                                ps.setString(5, opportunity.status());
                                ps.setString(6, id);
                                ps.executeUpdate();
                            }
                            return null;
                        })));
    }
    
    public Try<Void> delete(String id) {
        return DatabaseManager.getConnection()
            .flatMap(conn -> Try.withResources(() -> conn)
                .of(connection -> {
                    String sql = "DELETE FROM TblVolunteerOpportunities WHERE OpportunityID = ?";
                    try (PreparedStatement ps = connection.prepareStatement(sql)) {
                        ps.setString(1, id);
                        ps.executeUpdate();
                    }
                    return null;
                }));
    }
    
    private VolunteerOpportunity mapToVolunteerOpportunity(ResultSet rs) throws SQLException {
        return new VolunteerOpportunity(
            rs.getString("OpportunityID"),
            rs.getString("OrphanageID"),
            rs.getString("SkillRequired"),
            rs.getString("Location"),
            Option.of(rs.getString("TimeCommitment")),
            rs.getString("Status")
        );
    }
}