package com.orphanagehub.service;

import com.orphanagehub.dao.OrphanageDAO;
import com.orphanagehub.dao.UserDAO;
import com.orphanagehub.model.Orphanage;
import com.orphanagehub.model.User;
import java.sql.SQLException;
import java.util.List;

public class AdminService() {
  private final UserDAO userDAO = new UserDAO();
  private final OrphanageDAO orphanageDAO = new OrphanageDAO();

  public List<User> searchUsers(String searchTerm, String roleFilter) throws ServiceException() {
    try {
      return userDAO.findAll(searchTerm, roleFilter);
    } catch(SQLException e) {
      throw new ServiceException("Failed to search for users.", e);
    }
  }

  public boolean updateUserStatus(String userId, String newStatus) throws ServiceException() {
    try {
      return userDAO.updateUserStatus(userId, newStatus);
    } catch(SQLException e) {
      throw new ServiceException("Failed to update user status.", e);
    }
  }

  public List<Orphanage> getPendingOrphanages() throws ServiceException() {
    try {
      return orphanageDAO.findByStatus("Pending");
    } catch(SQLException e) {
      throw new ServiceException("Failed to load pending orphanages.", e);
    }
  }

  public boolean updateOrphanageVerification(String orphanageId, String newStatus) {
      throws ServiceException() {
    try {
      return orphanageDAO.updateVerificationStatus(orphanageId, newStatus);
    } catch(SQLException e) {
      throw new ServiceException("Failed to update orphanage status.", e);
    }
  }
}
}
