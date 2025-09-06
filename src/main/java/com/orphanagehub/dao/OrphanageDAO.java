package com.orphanagehub.dao;

import com.orphanagehub.model.Orphanage;
import com.orphanagehub.dao.DatabaseManager;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrphanageDAO {

    public Try<Orphanage> create(Orphanage orphanage) {
        return Try.of(() -> {
            String sql = "INSERT INTO TblOrphanages (OrphanageID, Name, Address, ContactPerson, ContactEmail, ContactPhone, VerificationStatus) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                String orphanageId = "ORP-" + UUID.randomUUID().toString().substring(0, 7).toUpperCase();
                ps.setString(1, orphanageId);
                ps.setString(2, orphanage.name());
                ps.setString(3, orphanage.address());
                ps.setString(4, orphanage.contactPerson());
                ps.setString(5, orphanage.contactEmail().getOrNull());
                ps.setString(6, orphanage.contactPhone().getOrNull());
                ps.setString(7, orphanage.verificationStatus());
                ps.executeUpdate();
                return orphanage.withOrphanageId(orphanageId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Try<Orphanage> update(Orphanage orphanage) {
        return Try.of(() -> {
            String sql = "UPDATE TblOrphanages SET Name = ?, Address = ?, ContactPerson = ?, ContactEmail = ?, ContactPhone = ?, VerificationStatus = ? WHERE OrphanageID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, orphanage.name());
                ps.setString(2, orphanage.address());
                ps.setString(3, orphanage.contactPerson());
                ps.setString(4, orphanage.contactEmail().getOrNull());
                ps.setString(5, orphanage.contactPhone().getOrNull());
                ps.setString(6, orphanage.verificationStatus());
                ps.setString(7, orphanage.orphanageId());
                ps.executeUpdate();
                return orphanage;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Try<Option<Orphanage>> findById(String orphanageId) {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblOrphanages WHERE OrphanageID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, orphanageId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return Option.of(mapRowToOrphanage(rs));
                } else {
                    return Option.none();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Try<List<Orphanage>> findAll() {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblOrphanages";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                List<Orphanage> orphanages = new ArrayList<>();
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    orphanages.add(mapRowToOrphanage(rs));
                }
                return orphanages;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Try<List<Orphanage>> findByVerificationStatus(String status) {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblOrphanages WHERE VerificationStatus = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                List<Orphanage> orphanages = new ArrayList<>();
                ps.setString(1, status);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    orphanages.add(mapRowToOrphanage(rs));
                }
                return orphanages;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Orphanage mapRowToOrphanage(ResultSet rs) throws SQLException {
        return new Orphanage(
            rs.getString("OrphanageID"),
            rs.getString("Name"),
            rs.getString("Address"),
            rs.getString("ContactPerson"),
            Option.of(rs.getString("ContactEmail")),
            Option.of(rs.getString("ContactPhone")),
            rs.getString("VerificationStatus")
        );
    }
}


