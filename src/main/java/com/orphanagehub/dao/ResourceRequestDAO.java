package com.orphanagehub.dao;

import com.orphanagehub.model.ResourceRequest;
import com.orphanagehub.util.DatabaseManager;
import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DAO for ResourceRequest operations.
 * 
 * @author OrphanageHub Team
 * @version 2.0
 * @since 2025-09-06
 */
public class ResourceRequestDAO {
    private static final Logger logger = LoggerFactory.getLogger(ResourceRequestDAO.class);
    
    public Try<ResourceRequest> create(ResourceRequest request) {
        String sql = """
            INSERT INTO TblResourceRequests (OrphanageID, ResourceType, ResourceDescription,
                Quantity, Unit, UrgencyLevel, NeededByDate, Status, EstimatedValue, Notes,
                ImagePath, CreatedBy) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
            
        return DatabaseManager.getConnection()
            .flatMap(conn -> Try.withResources(() -> conn)
                .of(c -> {
                    PreparedStatement stmt = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    stmt.setInt(1, request.orphanageId());
                    stmt.setString(2, request.resourceType());
                    stmt.setString(3, request.resourceDescription());
                    stmt.setDouble(4, request.quantity());
                    stmt.setString(5, request.unit().getOrNull());
                    stmt.setString(6, request.urgencyLevel());
                    stmt.setDate(7, request.neededByDate()
                        .map(Date::valueOf).getOrNull());
                    stmt.setString(8, request.status());
                    stmt.setObject(9, request.estimatedValue().getOrNull());
                    stmt.setString(10, request.notes().getOrNull());
                    stmt.setString(11, request.imagePath().getOrNull());
                    stmt.setInt(12, request.createdBy());
                    
                    stmt.executeUpdate();
                    
                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        return new ResourceRequest(
                            rs.getInt(1),
                            request.orphanageId(),
                            request.resourceType(),
                            request.resourceDescription(),
                            request.quantity(),
                            request.unit(),
                            request.urgencyLevel(),
                            request.requestDate(),
                            request.neededByDate(),
                            request.status(),
                            request.fulfilledDate(),
                            request.fulfilledBy(),
                            request.fulfillmentNotes(),
                            request.estimatedValue(),
                            request.actualValue(),
                            request.notes(),
                            request.imagePath(),
                            request.createdBy(),
                            request.modifiedDate(),
                            request.modifiedBy()
                        );
                    }
                    throw new SQLException("Failed to get generated ID");
                }));
    }
    
    public Try<ResourceRequest> update(ResourceRequest request) {
        String sql = """
            UPDATE TblResourceRequests 
            SET ResourceType = ?, ResourceDescription = ?, Quantity = ?, Unit = ?,
                UrgencyLevel = ?, NeededByDate = ?, Status = ?, FulfilledDate = ?,
                FulfilledBy = ?, FulfillmentNotes = ?, EstimatedValue = ?, ActualValue = ?,
                Notes = ?, ImagePath = ?, ModifiedDate = CURRENT_TIMESTAMP, ModifiedBy = ?
            WHERE RequestID = ?
            """;
            
        return DatabaseManager.getConnection()
            .flatMap(conn -> Try.withResources(() -> conn)
                .of(c -> {
                    PreparedStatement stmt = c.prepareStatement(sql);
                    stmt.setString(1, request.resourceType());
                    stmt.setString(2, request.resourceDescription());
                    stmt.setDouble(3, request.quantity());
                    stmt.setString(4, request.unit().getOrNull());
                    stmt.setString(5, request.urgencyLevel());
                    stmt.setDate(6, request.neededByDate()
                        .map(Date::valueOf).getOrNull());
                    stmt.setString(7, request.status());
                    stmt.setTimestamp(8, request.fulfilledDate()
                        .map(Timestamp::valueOf).getOrNull());
                    stmt.setObject(9, request.fulfilledBy().getOrNull());
                    stmt.setString(10, request.fulfillmentNotes().getOrNull());
                    stmt.setObject(11, request.estimatedValue().getOrNull());
                    stmt.setObject(12, request.actualValue().getOrNull());
                    stmt.setString(13, request.notes().getOrNull());
                    stmt.setString(14, request.imagePath().getOrNull());
                    stmt.setObject(15, request.modifiedBy().getOrNull());
                    stmt.setInt(16, request.requestId());
                    
                    stmt.executeUpdate();
                    return request;
                }));
    }
    
    public Try<Option<ResourceRequest>> findById(Integer requestId) {
        String sql = "SELECT * FROM TblResourceRequests WHERE RequestID = ?";
        
        return DatabaseManager.getConnection()
            .flatMap(conn -> Try.withResources(() -> conn)
                .of(c -> {
                    PreparedStatement stmt = c.prepareStatement(sql);
                    stmt.setInt(1, requestId);
                    ResultSet rs = stmt.executeQuery();
                    
                    if (rs.next()) {
                        return Option.of(mapToResourceRequest(rs));
                    }
                    return Option.<ResourceRequest>none();
                }));
    }
    
    public Try<List<ResourceRequest>> findByOrphanageId(Integer orphanageId) {
        String sql = "SELECT * FROM TblResourceRequests WHERE OrphanageID = ? ORDER BY RequestDate DESC";
        
        return DatabaseManager.getConnection()
            .flatMap(conn -> Try.withResources(() -> conn)
                .of(c -> {
                    PreparedStatement stmt = c.prepareStatement(sql);
                    stmt.setInt(1, orphanageId);
                    ResultSet rs = stmt.executeQuery();
                    
                    List<ResourceRequest> requests = List.empty();
                    while (rs.next()) {
                        requests = requests.append(mapToResourceRequest(rs));
                    }
                    return requests;
                }));
    }
    
    public Try<List<ResourceRequest>> findOpenRequests() {
        String sql = """
            SELECT * FROM TblResourceRequests 
            WHERE Status IN ('Open', 'In Progress')
            ORDER BY 
                CASE UrgencyLevel 
                    WHEN 'Critical' THEN 1 
                    WHEN 'High' THEN 2 
                    WHEN 'Medium' THEN 3 
                    WHEN 'Low' THEN 4 
                END,
                RequestDate DESC
            """;
            
        return DatabaseManager.getConnection()
            .flatMap(conn -> Try.withResources(() -> conn)
                .of(c -> {
                    Statement stmt = c.createStatement();
                    ResultSet rs = stmt.executeQuery(sql);
                    
                    List<ResourceRequest> requests = List.empty();
                    while (rs.next()) {
                        requests = requests.append(mapToResourceRequest(rs));
                    }
                    return requests;
                }));
    }
    
    public Try<List<ResourceRequest>> findUrgentRequests() {
        String sql = """
            SELECT * FROM TblResourceRequests 
            WHERE Status IN ('Open', 'In Progress')
            AND UrgencyLevel IN ('Critical', 'High')
            ORDER BY RequestDate DESC
            """;
            
        return DatabaseManager.getConnection()
            .flatMap(conn -> Try.withResources(() -> conn)
                .of(c -> {
                    Statement stmt = c.createStatement();
                    ResultSet rs = stmt.executeQuery(sql);
                    
                    List<ResourceRequest> requests = List.empty();
                    while (rs.next()) {
                        requests = requests.append(mapToResourceRequest(rs));
                    }
                    return requests;
                }));
    }
    
    private ResourceRequest mapToResourceRequest(ResultSet rs) throws SQLException {
        return new ResourceRequest(
            rs.getInt("RequestID"),
            rs.getInt("OrphanageID"),
            rs.getString("ResourceType"),
            rs.getString("ResourceDescription"),
            rs.getDouble("Quantity"),
            Option.of(rs.getString("Unit")),
            rs.getString("UrgencyLevel"),
            rs.getTimestamp("RequestDate").toLocalDateTime(),
            Option.of(rs.getDate("NeededByDate"))
                .map(Date::toLocalDate),
            rs.getString("Status"),
            Option.of(rs.getTimestamp("FulfilledDate"))
                .map(Timestamp::toLocalDateTime),
            Option.of(rs.getObject("FulfilledBy", Integer.class)),
            Option.of(rs.getString("FulfillmentNotes")),
            Option.of(rs.getObject("EstimatedValue", Double.class)),
            Option.of(rs.getObject("ActualValue", Double.class)),
            Option.of(rs.getString("Notes")),
            Option.of(rs.getString("ImagePath")),
            rs.getInt("CreatedBy"),
            Option.of(rs.getTimestamp("ModifiedDate"))
                .map(Timestamp::toLocalDateTime),
            Option.of(rs.getObject("ModifiedBy", Integer.class))
        );
    }
}