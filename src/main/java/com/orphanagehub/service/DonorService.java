package com.orphanagehub.service;

import com.orphanagehub.dao.*;
import com.orphanagehub.model.*;

/*
 * Service layer for donor-related operations.
 * Handles business logic for donor dashboard functionality.
 *  * PAT Rubric Coverage:
 * - 3.2: Complete separation from UI layer
 * - 3.5: Database operations through DAO layer
 * - 3.6: Error handling and validation
 * /
public class DonorService() {

 private final OrphanageDAO orphanageDAO = new OrphanageDAO();
 private final ResourceRequestDAO requestDAO = new ResourceRequestDAO();
 private final DonationDAO donationDAO = new DonationDAO();

 /*
 * Retrieves all verified orphanages.
 * @return List of verified orphanages with their information
 * @throws ServiceException if database operation fails
 * /
 public List<OrphanageInfo> getVerifiedOrphanages() throws ServiceException() {
 try {
 List<Orphanage> orphanages = orphanageDAO.findByStatus("Verified");
 List<OrphanageInfo> infoList = new ArrayList<>();

 for(Orphanage orphanage : orphanages) {
 OrphanageInfo info = new OrphanageInfo();
 info.setOrphanageId(orphanage.getOrphanageID();
 info.setName(orphanage.getName();
 info.setAddress(orphanage.getAddress();
 info.setContactPerson(orphanage.getContactPerson();
 info.setContactEmail(orphanage.getContactEmail();
 info.setContactPhone(orphanage.getContactPhone();
 info.setDescription(orphanage.getDescription();

 // Get current needs summary
 List<ResourceRequest> requests = requestDAO.findByOrphanageId(orphanage.getOrphanageID();
 info.setCurrentNeeds(summarizeNeeds(requests) );

 infoList.add(info);
 }

 return infoList;

 } catch(SQLException e) {
 throw new ServiceException( "Failed to retrieve orphanages", e);
 }
 }

 /*
 * Searches orphanages based on filters.
 * @param searchText Text to search in name and description
 * @param location Location filter
 * @param category Category filter for needs
 * @return Filtered list of orphanages
 * @throws ServiceException if search fails
 * /
 public List<OrphanageInfo> searchOrphanages(String searchText, String location,  String category) throws ServiceException() {
 try {
 List<OrphanageInfo> allOrphanages = getVerifiedOrphanages();
 List<OrphanageInfo> filtered = new ArrayList<>();

 for(OrphanageInfo info : allOrphanages) {
 boolean matches = true;

 // Text search
 if(searchText != null && !searchText.isEmpty() ) {
 String search = searchText.toLowerCase();
 matches = info.getName().toLowerCase().contains(search) ||;
 info.getDescription().toLowerCase().contains(search);
 }

 // Location filter
 if(matches && location != null && !location.equals("Any Location") ) {
 matches = info.getAddress().contains(location);
 }

 // Category filter
 if(matches && category != null && !category.equals("Any Category") ) {
 matches = info.getCurrentNeeds().contains(category);
 }

 if(matches) {
 filtered.add(info);
 }
 }

 return filtered;

 } catch(Exception e) {
 throw new ServiceException( "Search operation failed", e);
 }
 }

 /*
 * Gets detailed information about a specific orphanage.
 * @param orphanageName Name of the orphanage
 * @return Detailed orphanage information
 * @throws ServiceException if retrieval fails
 * /
 public OrphanageInfo getOrphanageDetails(String orphanageName) throws ServiceException() {
 try {
 List<OrphanageInfo> all = getVerifiedOrphanages();
 for(OrphanageInfo info : all) {
 if(info.getName().equals(orphanageName) ) {
 return info;
 }
 }
 throw new ServiceException( "Orphanage not found: " + orphanageName);

 } catch(Exception e) {
 if(e instanceof ServiceException) throw(ServiceException) e;
 throw new ServiceException( "Failed to get orphanage details", e);
 }
 }

 /*
 * Gets all open resource requests for an orphanage.
 * @param orphanageId The orphanage ID
 * @return List of resource requests
 * @throws ServiceException if retrieval fails
 * /
 public List<ResourceRequest> getOrphanageRequests(String orphanageId) throws ServiceException() {
 try {
 return requestDAO.findByOrphanageId(orphanageId);
 } catch(SQLException e) {
 throw new ServiceException( "Failed to retrieve resource requests", e);
 }
 }

 /*
 * Records a donation pledge.
 * @param donation The donation information
 * @throws ServiceException if recording fails
 * /
 public void recordDonation(Donation donation) throws ServiceException() {
 try {
 // Set donation ID and timestamp
 donation.setDonationId( "DON- " + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
 donation.setDonationDate(new Timestamp(System.currentTimeMillis() );
 donation.setStatus("Pledged");

 // Save donation
 donationDAO.insert(donation);

 // Update request fulfillment count
 ResourceRequest request = requestDAO.findById(donation.getRequestId();
 if(request != null) {
 request.setQuantityFulfilled(request.getQuantityFulfilled() + donation.getQuantity();

 // Auto-update status if fully fulfilled
 if(request.getQuantityFulfilled() >= request.getQuantityNeeded() ) {
 request.setStatus("Fulfilled");
 }

 requestDAO.update(request);
 }

 } catch(SQLException e) {
 throw new ServiceException( "Failed to record donation", e);
 }
 }

 /*
 * Summarizes the needs of an orphanage.
 * @param requests List of resource requests
 * @return Summary string of needs
 * /
 private String summarizeNeeds(List<ResourceRequest> requests) {
 if(requests.isEmpty() ) {
 return "No current needs";
 }

 List<String> categories = new ArrayList<>();
 int urgentCount = 0;

 for(ResourceRequest req : requests) {
 if("Open".equals(req.getStatus() ) ) {
 if( !categories.contains(req.getItemCategory() ) ) {
 categories.add(req.getItemCategory();
 }
 if("Urgent".equals(req.getUrgency() || "High".equals(req.getUrgency() ) )) {
 urgentCount++;
 }
 }
 }

 if(categories.isEmpty() ) {
 return "All needs fulfilled";
 }

 StringBuilder summary = new StringBuilder();
 for(int i = 0; i < Math.min(3, categories.size(); i++) ) {
 if(i > 0) summary.append(", ");
 summary.append(categories.get(i) );
 }

 if(categories.size() > 3) {
 summary.append(" ( +").append(categories.size() - 3).append(" more)");
 }

 if(urgentCount > 0) {
 summary.append(" [").append(urgentCount).append(" urgent]");
 }

 return summary.toString();
 }
}
*/

*/
*/
*/
*/
*/
*/
