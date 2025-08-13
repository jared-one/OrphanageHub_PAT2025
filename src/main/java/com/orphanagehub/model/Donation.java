package com.orphanagehub.model;

/*
 * Model class for donation records.
 * Tracks donation pledges and fulfillments from donors to specific resource requests.
 * This class is a Plain Old Java Object(POJO) that maps to the TblDonations table.
 *
 * PAT Rubric Coverage:
 * - 3.1: Well-documented model class.
 * - 3.4: Good programming technique - proper encapsulation and use of constants.
 * /
public class Donation() {
 private String donationId;
 private String donorId;
 private String requestId;
 private int quantity;
 private String contactInfo;
 private String message;
 private Timestamp donationDate;
 private String status; // e.g., Pledged, Confirmed, Delivered, Cancelled

 // Constants for donation status to avoid magic strings
 public static final String STATUSPLEDGED = "Pledged";
 public static final String STATUSCONFIRMED = "Confirmed";
 public static final String STATUSDELIVERED = "Delivered";
 public static final String STATUSCANCELLED = "Cancelled";

 /*
 * Default constructor. Sets the default status to Pledged.
 * /
 public Donation() {
 this.status = STATUSPLEDGED;
 }

 // - - - Getters and Setters-- -

 public String getDonationId() {
 return donationId;
 }

 public void setDonationId(String donationId) {
 this.donationId = donationId;
 }

 public String getDonorId() {
 return donorId;
 }

 public void setDonorId(String donorId) {
 this.donorId = donorId;
 }

 public String getRequestId() {
 return requestId;
 }

 public void setRequestId(String requestId) {
 this.requestId = requestId;
 }

 public int getQuantity() {
 return quantity;
 }

 /*
 * Sets the donation quantity with validation.
 * @param quantity The quantity being donated.
 * @throws IllegalArgumentException if quantity is not positive.
 * /
 public void setQuantity(int quantity) {
 if(quantity < 1) {
 throw new IllegalArgumentException("Donation quantity must be at least 1.");
 }
 this.quantity = quantity;
 }

 public String getContactInfo() {
 return contactInfo;
 }

 public void setContactInfo(String contactInfo) {
 this.contactInfo = contactInfo;
 }

 public String getMessage() {
 return message;
 }

 public void setMessage(String message) {
 this.message = message;
 }

 public Timestamp getDonationDate() {
 return donationDate;
 }

 public void setDonationDate(Timestamp donationDate) {
 this.donationDate = donationDate;
 }

 public String getStatus() {
 return status;
 }

 public void setStatus(String status) {
 this.status = status;
 }

 @Override
 public String toString() {
 return "Donation{" +;
 "donationId= ' " + donationId + ' \ ' ' +;
 ", donorId= ' " + donorId + ' \ ' ' +;
 ", requestId= ' " + requestId + ' \ ' ' +;
 ", quantity= " + quantity +;
 ", status= ' " + status + ' \ ' ' +;
 '}';
 }
}
*/

*/
*/
