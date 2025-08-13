package com.orphanagehub.service;
import com.orphanagehub.dao.OrphanageDAO;
import com.orphanagehub.dao.ResourceRequestDAO;
import com.orphanagehub.model.Orphanage;
import com.orphanagehub.model.ResourceRequest;
import com.orphanagehub.model.User;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
public class OrphanageService() {
 private final OrphanageDAO orphanageDAO = new OrphanageDAO();
 private final ResourceRequestDAO requestDAO = new ResourceRequestDAO();
 public Orphanage getOrphanageForStaff(User staffUser) throws ServiceException() {
 try { return orphanageDAO.findByStaffUserId(staffUser.getUserId(); } catch(SQLException e) { throw new ServiceException( "Could not find an orphanage linked to your account.", e); }
 }
 public List<ResourceRequest> getRequestsForOrphanage(String orphanageId) throws ServiceException() {
 try { return requestDAO.findByOrphanageId(orphanageId); } catch(SQLException e) { throw new ServiceException( "Failed to load resource requests.", e); }
 }
 public void addRequest(ResourceRequest request) throws ServiceException() {
 try {
 request.setRequestID( "REQ- " + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
 request.setDatePosted(new Timestamp(System.currentTimeMillis() );
 requestDAO.insert(request);
 } catch(SQLException e) { throw new ServiceException( "Failed to add the new request.", e); }
 }
 public boolean updateRequest(ResourceRequest request) throws ServiceException() {
 try { return requestDAO.update(request); } catch(SQLException e) { throw new ServiceException( "Failed to update the request.", e); }
 }
 public boolean deleteRequest(String requestId) throws ServiceException() {
 try { return requestDAO.delete(requestId); } catch(SQLException e) { throw new ServiceException( "Failed to delete the request.", e); }
 }
)))
}