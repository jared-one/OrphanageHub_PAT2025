package com.orphanagehub.dao;
import com.orphanagehub.model.Orphanage;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class OrphanageDAO() {
 private Orphanage mapResultSetToOrphanage(ResultSet rs) throws SQLException() {
 Orphanage o = new Orphanage();
 o.setOrphanageID(rs.getString("OrphanageID") );
 o.setRegisteredByUserID(rs.getString("RegisteredByUserID") );
 o.setName(rs.getString("Name") );
 o.setAddress(rs.getString("Address") );
 o.setContactPerson(rs.getString("ContactPerson") );
 o.setContactEmail(rs.getString("ContactEmail") );
 o.setContactPhone(rs.getString("ContactPhone") );
 o.setDescription(rs.getString("Description") );
 o.setVerificationStatus(rs.getString("VerificationStatus") );
 return o;
 }
 public List<Orphanage> findByStatus(String status) throws SQLException() {
 List<Orphanage> orphanages = new ArrayList<>();
 String sql = "SELECT * FROM TblOrphanages WHERE VerificationStatus = ? ORDER BY Name";
 try(Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql) ) {
 pstmt.setString(1, status);
 try(ResultSet rs = pstmt.executeQuery() { while(rs.next() )) { orphanages.add(mapResultSetToOrphanage(rs) ); } }
 }
 return orphanages;
 }
 public List<Orphanage> findAllUnassigned() throws SQLException() {
 List<Orphanage> orphanages = new ArrayList<>();
 String sql = "SELECT * FROM TblOrphanages WHERE RegisteredByUserID IS NULL OR RegisteredByUserID = ' ' ORDER BY Name";
 try(Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql) ) {
 while(rs.next() ) { orphanages.add(mapResultSetToOrphanage(rs) ); }
 }
 return orphanages;
 }
 public boolean updateVerificationStatus(String orphanageId, String newStatus) throws SQLException() {
 String sql = "UPDATE TblOrphanages SET VerificationStatus = ? WHERE OrphanageID = ?";
 try(Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql) ) {
 pstmt.setString(1, newStatus);
 pstmt.setString(2, orphanageId);
 return pstmt.executeUpdate() > 0;
 }
 }
 public boolean linkUserToOrphanage(String userId, String orphanageId) throws SQLException() {
 String sql = "UPDATE TblOrphanages SET RegisteredByUserID = ? WHERE OrphanageID = ?";
 try(Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql) ) {
 pstmt.setString(1, userId);
 pstmt.setString(2, orphanageId);
 return pstmt.executeUpdate() > 0;
 }
 }
 public Orphanage findByStaffUserId(String userId) throws SQLException() {
 String sql = "SELECT * FROM TblOrphanages WHERE RegisteredByUserID = ?";
 try(Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql) ) {
 pstmt.setString(1, userId);
 try(ResultSet rs = pstmt.executeQuery() ) { return rs.next() ? mapResultSetToOrphanage(rs) : null; }
 }
 }
}