package com.orphanagehub.dao;

import com.orphanagehub.model.ResourceRequest;
import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

public class ResourceRequestDAO {
    
    public Try<Void> create(ResourceRequest request) {
        return Try.run(() -> {
            String sql = "INSERT INTO TblResourceRequests (RequestID, OrphanageID, UserID, ItemCategory, ItemDescription, QuantityNeeded, QuantityFulfilled, Urgency, Status, DatePosted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, UUID.randomUUID().toString().substring(0, 10));
                ps.setString(2, request.orphanageId());
                ps.setString(3, request.userId());
                ps.setString(4, request.itemCategory());
                ps.setString(5, request.itemDescription());
                ps.setInt(6, request.quantityNeeded());
                ps.setInt(7, request.quantityFulfilled());
                ps.setString(8, request.urgency());
                ps.setString(9, request.status());
                ps.setTimestamp(10, request.datePosted());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    public Try<List<ResourceRequest>> findByOrphanageId(String orphanageId) {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblResourceRequests WHERE OrphanageID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                List<ResourceRequest> requests = List.empty();
                ps.setString(1, orphanageId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    requests = requests.append(mapToResourceRequest(rs));
                }
                return requests;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    public Try<Option<ResourceRequest>> findById(String requestId) {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblResourceRequests WHERE RequestID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, requestId);
                ResultSet rs = ps.executeQuery();
                return rs.next() ? Option.of(mapToResourceRequest(rs)) : Option.<ResourceRequest>none();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    public Try<Void> update(ResourceRequest request) {
        return Try.run(() -> {
            String sql = "UPDATE TblResourceRequests SET ItemDescription=?, QuantityNeeded=?, QuantityFulfilled=?, Urgency=?, Status=? WHERE RequestID=?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, request.itemDescription());
                ps.setInt(2, request.quantityNeeded());
                ps.setInt(3, request.quantityFulfilled());
                ps.setString(4, request.urgency());
                ps.setString(5, request.status());
                ps.setString(6, request.requestId());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    public Try<Void> delete(String requestId) {
        return Try.run(() -> {
            String sql = "DELETE FROM TblResourceRequests WHERE RequestID=?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, requestId);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    private static ResourceRequest mapToResourceRequest(ResultSet rs) throws SQLException {
        return new ResourceRequest(
            rs.getString("RequestID"),
            rs.getString("OrphanageID"),
            rs.getString("UserID"),
            rs.getString("ItemCategory"),
            rs.getString("ItemDescription"),
            rs.getInt("QuantityNeeded"),
            rs.getInt("QuantityFulfilled"),
            rs.getString("Urgency"),
            rs.getString("Status"),
            rs.getTimestamp("DatePosted")
        );
    }
}