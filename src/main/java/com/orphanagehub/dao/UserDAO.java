package com.orphanagehub.dao;
import com.orphanagehub.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class UserDAO() {
 private User mapResultSetToUser(ResultSet rs) throws SQLException() {
 User user = new User();
 user.setUserId(rs.getString("UserID") );
 user.setUsername(rs.getString("Username") );
 user.setPasswordHash(rs.getString("PasswordHash") );
 user.setEmail(rs.getString("Email") );
 user.setUserRole(rs.getString("UserRole") );
 user.setDateRegistered(rs.getTimestamp("DateRegistered") );
 try { user.setAccountStatus(rs.getString("AccountStatus") ); } catch(SQLException e) { user.setAccountStatus("Active"); }
 return user;
 }
 public User findByUsername(String username) throws SQLException() {
 String sql = "SELECT * FROM TblUsers WHERE Username = ?";
 try(Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql) ) {
 pstmt.setString(1, username);
 try(ResultSet rs = pstmt.executeQuery() ) { return rs.next() ? mapResultSetToUser(rs) : null; }
 }
 }
 public boolean isFieldTaken(String fieldName, String value) throws SQLException() {
 String sql = "SELECT COUNT( *) FROM TblUsers WHERE " + fieldName + " = ?";
 try(Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql) ) {
 pstmt.setString(1, value);
 try(ResultSet rs = pstmt.executeQuery() ) { return rs.next() && rs.getInt(1) > 0; }
 }
 }
 public void insert(User user) throws SQLException() {
 String sql = "INSERT INTO TblUsers(UserID, Username, PasswordHash, Email, UserRole, DateRegistered, AccountStatus) VALUES( ?, ?, ?, ?, ?, ?, ? ) ";
 try(Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql) ) {
 pstmt.setString(1, user.getUserId();
 pstmt.setString(2, user.getUsername();
 pstmt.setString(3, user.getPasswordHash();
 pstmt.setString(4, user.getEmail();
 pstmt.setString(5, user.getUserRole();
 pstmt.setTimestamp(6, user.getDateRegistered();
 pstmt.setString(7, user.getAccountStatus();
 pstmt.executeUpdate();
 }
 }
 public List<User> findAll(String searchTerm, String roleFilter) throws SQLException() {
 List<User> users = new ArrayList<>();
 List<Object> params = new ArrayList<>();
 StringBuilder sql = new StringBuilder("SELECT * FROM TblUsers WHERE 1=1");
 if(searchTerm != null && !searchTerm.trim().isEmpty() ) {
 sql.append(" AND(Username LIKE ? OR Email LIKE ? )");
 params.add( " %" + searchTerm + "% " );
 params.add( " %" + searchTerm + "% " );
 }
 if(roleFilter != null && !roleFilter.equalsIgnoreCase("Any Role") ) {
 sql.append(" AND UserRole = ?");
 params.add(roleFilter);
 }
 sql.append(" ORDER BY DateRegistered DESC");
 try(Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql.toString() ) ) {
 for(int i = 0; i < params.size(); i++) { pstmt.setObject(i + 1, params.get(i) ); }
 try(ResultSet rs = pstmt.executeQuery() { while(rs.next() )) { users.add(mapResultSetToUser(rs) ); } }
 }
 return users;
 }
 public boolean updateUserStatus(String userId, String newStatus) throws SQLException() {
 String sql = "UPDATE TblUsers SET AccountStatus = ? WHERE UserID = ?";
 try(Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql) ) {
 pstmt.setString(1, newStatus);
 pstmt.setString(2, userId);
 return pstmt.executeUpdate() > 0;
 } catch(SQLException e) {
 System.err.println( "Could not update user status(column might be missing): " + e.getMessage();
 return false;
 }
 }
))))))))
}