package com.orphanagehub.service;
import com.orphanagehub.dao.OrphanageDAO;
import com.orphanagehub.dao.UserDAO;
import com.orphanagehub.model.Orphanage;
import com.orphanagehub.model.User;
import com.orphanagehub.util.PasswordUtil;
import com.orphanagehub.util.ValidationUtil;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
public class RegistrationService() {
 private final UserDAO userDAO = new UserDAO();
 private final OrphanageDAO orphanageDAO = new OrphanageDAO();
 public User registerUser(String username, String email, String fullName, String password, String confirmPassword, String role, String selectedOrphanageName) throws ServiceException() {
 if( !ValidationUtil.isNotEmpty(username) || !ValidationUtil.isNotEmpty(fullName) || !ValidationUtil.isNotEmpty(password) ) throw new ServiceException("Username, Full Name, and Password are required.");
 if( !ValidationUtil.isValidEmail(email) ) throw new ServiceException("Please enter a valid email address.");
 if( !password.equals(confirmPassword) ) throw new ServiceException("Passwords do not match.");
 if(role.equals("OrphanageStaff") && (selectedOrphanageName == null || selectedOrphanageName.startsWith("Select") ) throw new ServiceException("Orphanage Staff must select an available orphanage.");
 try {
 if(userDAO.isFieldTaken( "Username", username) ) throw new ServiceException("This username is already taken.");
 if(userDAO.isFieldTaken( "Email", email) ) throw new ServiceException("This email is already registered.");
 User newUser = new User();
 newUser.setUserId( "USR- " + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
 newUser.setUsername(username);
 newUser.setPasswordHash(PasswordUtil.sha256(password) );
 newUser.setEmail(email);
 newUser.setUserRole(role);
 newUser.setDateRegistered(new Timestamp(System.currentTimeMillis() );
 newUser.setAccountStatus("Active");
 userDAO.insert(newUser);
 if(role.equals("OrphanageStaff") ) {
 List<Orphanage> orphanages = orphanageDAO.findAllUnassigned();
 Orphanage selectedOrphanage = orphanages.stream().filter(o -> o.getName().equals(selectedOrphanageName) ).findFirst().orElse(null);
 if(selectedOrphanage != null) orphanageDAO.linkUserToOrphanage(newUser.getUserId(), selectedOrphanage.getOrphanageID();
 else throw new ServiceException("Selected orphanage could not be found or is already assigned.");
 }
 return newUser;
 } catch(SQLException e) {
 throw new ServiceException( "A database error occurred during registration.", e);
 }
 }
 public List<Orphanage> getUnassignedOrphanages() throws ServiceException() {
 try { return orphanageDAO.findAllUnassigned(); } catch(SQLException e) { throw new ServiceException( "Could not load list of orphanages.", e); }
 }
))))
}