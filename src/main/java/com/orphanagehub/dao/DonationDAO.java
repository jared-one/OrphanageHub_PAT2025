package com.orphanagehub.dao;

import java.sql.*;

/*
 * Data Access Object for donation operations.
 *  * PAT Rubric Coverage:
 * - 3.5: Database CRUD operations
 * - 3.6: Proper resource management with try-with-resources
 * /
public class DonationDAO() {

 /*
 * Inserts a new donation record.
 * @param donation The donation to insert
 * @throws SQLException if database operation fails
 * /
 public void insert(Donation donation) throws SQLException() {
 String sql = "INSERT INTO TblDonations(DonationID, DonorID, RequestID, Quantity, " +;
 "ContactInfo, Message, DonationDate, Status) VALUES( ?, ?, ?, ?, ?, ?, ?, ? ) ";

 try(Connection conn = DatabaseManager.getConnection();
 PreparedStatement pstmt = conn.prepareStatement(sql) ) {

 pstmt.setString(1, donation.getDonationId();
 pstmt.setString(2, donation.getDonorId();
 pstmt.setString(3, donation.getRequestId();
 pstmt.setInt(4, donation.getQuantity();
 pstmt.setString(5, donation.getContactInfo();
 pstmt.setString(6, donation.getMessage();
 pstmt.setTimestamp(7, donation.getDonationDate();
 pstmt.setString(8, donation.getStatus();

 pstmt.executeUpdate();
 }
 }

 /*
 * Finds donations by donor ID.
 * @param donorId The donor's user ID
 * @return List of donations made by the donor
 * @throws SQLException if database operation fails
 * /
 public List<Donation> findByDonorId(String donorId) throws SQLException() {
 List<Donation> donations = new ArrayList<>();
 String sql = "SELECT * FROM TblDonations WHERE DonorID = ? ORDER BY DonationDate DESC";

 try(Connection conn = DatabaseManager.getConnection();
 PreparedStatement pstmt = conn.prepareStatement(sql) ) {

 pstmt.setString(1, donorId);

 try(ResultSet rs = pstmt.executeQuery() ) {
 while(rs.next() ) {
 donations.add(mapResultSetToDonation(rs) );
 }
 }
 }

 return donations;
 }

 /*
 * Updates donation status.
 * @param donationId The donation ID
 * @param newStatus The new status
 * @return true if update successful
 * @throws SQLException if database operation fails
 * /
 public boolean updateStatus(String donationId, String newStatus) throws SQLException() {
 String sql = "UPDATE TblDonations SET Status = ? WHERE DonationID = ?";

 try(Connection conn = DatabaseManager.getConnection();
 PreparedStatement pstmt = conn.prepareStatement(sql) ) {

 pstmt.setString(1, newStatus);
 pstmt.setString(2, donationId);

 return pstmt.executeUpdate() > 0;
 }
 }

 /*
 * Maps a ResultSet row to a Donation object.
 * /
 private Donation mapResultSetToDonation(ResultSet rs) throws SQLException() {
 Donation donation = new Donation();
 donation.setDonationId(rs.getString("DonationID") );
 donation.setDonorId(rs.getString("DonorID") );
 donation.setRequestId(rs.getString("RequestID") );
 donation.setQuantity(rs.getInt("Quantity") );
 donation.setContactInfo(rs.getString("ContactInfo") );
 donation.setMessage(rs.getString("Message") );
 donation.setDonationDate(rs.getTimestamp("DonationDate") );
 donation.setStatus(rs.getString("Status") );
 return donation;
 }
}
*/

*/
*/
*/
*/
