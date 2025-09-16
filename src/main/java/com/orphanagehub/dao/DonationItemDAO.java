package com.orphanagehub.dao;

import com.orphanagehub.model.DonationItem;
import io.vavr.control.Try;
import io.vavr.collection.List;
import io.vavr.control.Option;
import java.sql.*;
import java.time.LocalDateTime;

public class DonationItemDAO {
    
    public Try<List<DonationItem>> createBatch(Integer donationId, List<DonationItem> items) {
        return Try.of(() -> {
            String sql = "INSERT INTO TblDonationItems (DonationID, ItemType, ItemDescription, " +
                        "Quantity, Unit, EstimatedValue, CreatedDate) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                for (DonationItem item : items) {
                    ps.setInt(1, donationId);
                    ps.setString(2, item.itemType());
                    ps.setString(3, item.itemDescription());
                    ps.setDouble(4, item.quantity());
                    ps.setString(5, item.unit().getOrNull());
                    ps.setObject(6, item.estimatedValue().getOrNull());
                    ps.setTimestamp(7, Timestamp.valueOf(item.createdDate()));
                    ps.addBatch();
                }
                ps.executeBatch();
                return items;
            }
        });
    }
    
    public Try<List<DonationItem>> findByDonation(Integer donationId) {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblDonationItems WHERE DonationID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, donationId);
                ResultSet rs = ps.executeQuery();
                List<DonationItem> items = List.empty();
                while (rs.next()) {
                    items = items.append(mapToDonationItem(rs));
                }
                return items;
            }
        });
    }
    
    private DonationItem mapToDonationItem(ResultSet rs) throws SQLException {
        return new DonationItem(
            rs.getObject("ItemID", Integer.class),
            rs.getInt("DonationID"),
            rs.getString("ItemType"),
            rs.getString("ItemDescription"),
            rs.getDouble("Quantity"),
            Option.of(rs.getString("Unit")),
            Option.of(rs.getObject("EstimatedValue", Double.class)),
            rs.getTimestamp("CreatedDate").toLocalDateTime()
        );
    }
}