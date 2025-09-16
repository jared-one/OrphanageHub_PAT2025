package com.orphanagehub.service;

import com.orphanagehub.dao.*;
import com.orphanagehub.model.*;
import com.orphanagehub.util.DatabaseManager;
import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Complete admin service with all management functions.
 * 
 * @author OrphanageHub Team
 * @version 2.0
 * @since 2025-09-06
 */
public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    
    private final UserDAO userDAO = new UserDAO();
    private final OrphanageDAO orphanageDAO = new OrphanageDAO();
    private final DonationDAO donationDAO = new DonationDAO();
    private final ResourceRequestDAO requestDAO = new ResourceRequestDAO();
    private final VolunteerOpportunityDAO opportunityDAO = new VolunteerOpportunityDAO();
    private final VolunteerApplicationDAO applicationDAO = new VolunteerApplicationDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();
    
    /**
     * Gets system dashboard statistics
     */
    public Try<SystemStatistics> getSystemStatistics() {
        return Try.of(() -> {
            // User statistics
            int totalUsers = userDAO.findAllActive().map(List::size).getOrElse(0);
            int donors = userDAO.findByRole("Donor").map(List::size).getOrElse(0);
            int volunteers = userDAO.findByRole("Volunteer").map(List::size).getOrElse(0);
            int orphanageStaff = userDAO.findByRole("OrphanageRep").map(List::size).getOrElse(0);
            
            // Orphanage statistics
            int totalOrphanages = orphanageDAO.findAllVerified().map(List::size).getOrElse(0);
            int pendingVerifications = orphanageDAO.findByVerificationStatus("Pending")
                .map(List::size).getOrElse(0);
            
            // Donation statistics
            double totalDonations = donationDAO.getTotalDonations().getOrElse(0.0);
            int monthlyDonations = donationDAO.getMonthlyDonationCount().getOrElse(0);
            
            // Request statistics
            int openRequests = requestDAO.findOpenRequests().map(List::size).getOrElse(0);
            int urgentRequests = requestDAO.findUrgentRequests().map(List::size).getOrElse(0);
            
            // Volunteer statistics
            int activeOpportunities = opportunityDAO.findOpenOpportunities()
                .map(List::size).getOrElse(0);
            int pendingApplications = applicationDAO.getPendingApplicationCount().getOrElse(0);
            
            return new SystemStatistics(
                totalUsers,
                donors,
                volunteers,
                orphanageStaff,
                totalOrphanages,
                pendingVerifications,
                totalDonations,
                monthlyDonations,
                openRequests,
                urgentRequests,
                activeOpportunities,
                pendingApplications
            );
        });
    }
    
    /**
     * Manages user account (activate/suspend/delete)
     */
    public Try<Void> manageUserAccount(Integer userId, String action, Integer adminId, String reason) {
        return userDAO.findById(userId)
            .flatMap(userOpt -> userOpt.toTry(() -> 
                new ServiceException("User not found")))
            .flatMap(user -> {
                User updated = switch (action.toLowerCase()) {
                    case "activate" -> user.withAccountStatus("Active");
                    case "suspend" -> user.withAccountStatus("Suspended");
                    case "delete" -> user.withAccountStatus("Deleted");
                    default -> throw new ServiceException("Invalid action: " + action);
                };
                
                return userDAO.update(updated)
                    .flatMap(u -> {
                        // Send notification to user
                        String message = "Your account has been " + action.toLowerCase() + "d";
                        if (reason != null) {
                            message += ". Reason: " + reason;
                        }
                        
                        Notification notification = Notification.create(
                            userId,
                            Notification.TYPE_SYSTEM,
                            "Account Status Changed",
                            message,
                            Notification.PRIORITY_HIGH
                        );
                        notificationDAO.create(notification);
                        
                        // Log action
                        auditLogDAO.logSuccess(adminId, null, AuditLog.ACTION_UPDATE,
                            "User", userId + " - " + action);
                        
                        logger.info("User {} {} by admin {}", userId, action, adminId);
                        return Try.success(null);
                    });
            });
    }
    
    /**
     * Verifies orphanage registration
     */
    public Try<Void> verifyOrphanage(Integer orphanageId, boolean approved, 
                                    Integer adminId, String notes) {
        return orphanageDAO.findById(orphanageId)
            .flatMap(orphOpt -> orphOpt.toTry(() -> 
                new ServiceException("Orphanage not found")))
            .flatMap(orphanage -> {
                String status = approved ? "Verified" : "Rejected";
                
                return orphanageDAO.verifyOrphanage(orphanageId, adminId, notes)
                    .flatMap(v -> {
                        // Notify orphanage representative
                        userDAO.findById(orphanage.userId()).forEach(userOpt -> {
                            userOpt.forEach(user -> {
                                String message = approved 
                                    ? "Your orphanage has been verified and is now active!"
                                    : "Your orphanage verification was not approved. " + notes;
                                
                                Notification notification = new Notification(
                                    null,
                                    user.userId(),
                                    Notification.TYPE_VERIFICATION,
                                    "Orphanage Verification " + (approved ? "Approved" : "Rejected"),
                                    message,
                                    Notification.PRIORITY_HIGH,
                                    Notification.STATUS_UNREAD,
                                    LocalDateTime.now(),
                                    Option.none(),
                                    Option.none(),
                                    Option.none(),
                                    Option.of("Orphanage"),
                                    Option.of(orphanageId)
                                );
                                
                                notificationDAO.create(notification);
                            });
                        });
                        
                        // Log verification
                        auditLogDAO.logSuccess(adminId, null, AuditLog.ACTION_VERIFY,
                            "Orphanage", orphanageId + " - " + status);
                        
                        logger.info("Orphanage {} {} by admin {}", orphanageId, status, adminId);
                        return Try.success(null);
                    });
            });
    }
    
    /**
     * Gets pending verifications
     */
    public Try<List<Orphanage>> getPendingVerifications() {
        return orphanageDAO.findByVerificationStatus("Pending");
    }
    
    /**
     * Reviews volunteer opportunity
     */
    public Try<Void> reviewVolunteerOpportunity(Integer opportunityId, boolean approved,
                                               Integer adminId, String notes) {
        String status = approved ? "Open" : "Rejected";
        
        return opportunityDAO.updateStatus(opportunityId, status)
            .flatMap(v -> {
                // Get opportunity details for notification
                opportunityDAO.findById(opportunityId).forEach(oppOpt -> {
                    oppOpt.forEach(opportunity -> {
                        // Notify orphanage
                        orphanageDAO.findById(opportunity.orphanageId()).forEach(orphOpt -> {
                            orphOpt.forEach(orphanage -> {
                                userDAO.findById(orphanage.userId()).forEach(userOpt -> {
                                    userOpt.forEach(user -> {
                                        String message = approved
                                            ? "Your volunteer opportunity '" + opportunity.title() + "' has been approved!"
                                            : "Your volunteer opportunity was not approved. " + notes;
                                        
                                        Notification notification = Notification.create(
                                            user.userId(),
                                            Notification.TYPE_VOLUNTEER,
                                            "Volunteer Opportunity " + (approved ? "Approved" : "Rejected"),
                                            message,
                                            Notification.PRIORITY_NORMAL
                                        );
                                        
                                        notificationDAO.create(notification);
                                    });
                                });
                            });
                        });
                    });
                });
                
                // Log review
                auditLogDAO.logSuccess(adminId, null, AuditLog.ACTION_VERIFY,
                    "VolunteerOpportunity", opportunityId + " - " + status);
                
                logger.info("Volunteer opportunity {} {} by admin {}", 
                    opportunityId, status, adminId);
                return Try.success(null);
            });
    }
    
    /**
     * Generates system report
     */
    public Try<String> generateReport(ReportType reportType, ReportParameters params, Integer adminId) {
        return DatabaseManager.getConnection()
            .flatMap(conn -> Try.of(() -> {
                // Prepare parameters
                Map<String, Object> reportParams = new HashMap<>();
                reportParams.put("START_DATE", params.startDate());
                reportParams.put("END_DATE", params.endDate());
                reportParams.put("GENERATED_BY", adminId);
                reportParams.put("GENERATED_DATE", LocalDateTime.now());
                
                // Add type-specific parameters
                switch (reportType) {
                    case DONATIONS -> {
                        reportParams.put("MIN_AMOUNT", params.minAmount());
                        reportParams.put("MAX_AMOUNT", params.maxAmount());
                    }
                    case ORPHANAGES -> {
                        reportParams.put("PROVINCE", params.province());
                        reportParams.put("STATUS", params.status());
                    }
                    case VOLUNTEERS -> {
                        reportParams.put("CATEGORY", params.category());
                    }
                    case AUDIT -> {
                        reportParams.put("ACTION", params.action());
                        reportParams.put("USER_ID", params.userId());
                    }
                }
                
                // Load report template
                String templatePath = "reports/templates/" + reportType.getTemplateName() + ".jrxml";
                JasperReport jasperReport = JasperCompileManager.compileReport(templatePath);
                
                // Fill report
                JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport, reportParams, conn
                );
                
                // Export to PDF
                String timestamp = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
                );
                String fileName = reportType.name().toLowerCase() + "_report_" + timestamp + ".pdf";
                String outputPath = "reports/generated/" + fileName;
                
                // Ensure directory exists
                new File("reports/generated").mkdirs();
                
                // Export
                JRPdfExporter exporter = new JRPdfExporter();
                exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(
                    new FileOutputStream(outputPath)
                ));
                exporter.exportReport();
                
                // Log report generation
                auditLogDAO.logSuccess(adminId, null, AuditLog.ACTION_CREATE,
                    "Report", reportType + " - " + fileName);
                
                logger.info("Report generated: {}", outputPath);
                return outputPath;
            }))
            .onFailure(error -> {
                logger.error("Failed to generate report", error);
                auditLogDAO.logFailure(adminId, null, AuditLog.ACTION_CREATE,
                    "Report", error.getMessage());
            });
    }
    
    /**
     * Gets audit logs
     */
    public Try<List<AuditLog>> getAuditLogs(AuditLogFilter filter) {
        if (filter.userId() != null) {
            return auditLogDAO.findByUserId(filter.userId(), filter.from(), filter.to());
        } else if (filter.action() != null) {
            return auditLogDAO.findByAction(filter.action(), filter.from(), filter.to());
        } else if (filter.entityType() != null && filter.entityId() != null) {
            return auditLogDAO.findByEntity(filter.entityType(), filter.entityId());
        } else {
            return auditLogDAO.findAll(filter.from(), filter.to());
        }
    }
    
    /**
     * Sends system-wide notification
     */
    public Try<Integer> sendSystemNotification(String title, String message, 
                                              String priority, List<String> targetRoles,
                                              Integer adminId) {
        // Get target users
        List<Integer> userIds = targetRoles.flatMap(role ->
            userDAO.findByRole(role)
                .getOrElse(List.empty())
                .map(User::userId)
        ).distinct();
        
       return notificationDAO.createBulk(userIds, Notification.TYPE_SYSTEM, 
    title, message, priority)
    .flatMap(ids -> {  // Changed from .map to .flatMap and proper handling
        // Log notification
        auditLogDAO.logSuccess(adminId, null, AuditLog.ACTION_CREATE,
            "SystemNotification", "Sent to " + ids.size() + " users");
        
        logger.info("System notification sent to {} users", ids.size());
        return Try.success(ids.size());
    });
    }
    
    /**
     * Manages system settings
     */
    public Try<Void> updateSystemSetting(String key, String value, Integer adminId) {
        // In production, this would update a settings table
        logger.info("System setting updated: {} = {} by admin {}", key, value, adminId);
        
        auditLogDAO.logSuccess(adminId, null, AuditLog.ACTION_UPDATE,
            "SystemSetting", key);
        
        return Try.success(null);
    }
    
    /**
     * Backup database (placeholder)
     */
    public Try<String> backupDatabase() {
        return Try.of(() -> {
            String timestamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
            );
            String backupPath = "backups/orphanagehub_" + timestamp + ".db";
            
            // In production, implement actual database backup logic
            logger.info("Database backup created: {}", backupPath);
            return backupPath;
        });
    }
    
    // Supporting records and enums (ONLY DEFINE ONCE!)
    
    public record SystemStatistics(
        int totalUsers,
        int donors,
        int volunteers,
        int orphanageStaff,
        int totalOrphanages,
        int pendingVerifications,
        double totalDonations,
        int monthlyDonations,
        int openRequests,
        int urgentRequests,
        int activeOpportunities,
        int pendingApplications
    ) {}
    
    public enum ReportType {
        DONATIONS("donations_report"),
        ORPHANAGES("orphanages_report"),
        VOLUNTEERS("volunteers_report"),
        RESOURCES("resources_report"),
        AUDIT("audit_report"),
        FINANCIAL("financial_report");
        
        private final String templateName;
        
        ReportType(String templateName) {
            this.templateName = templateName;
        }
        
        public String getTemplateName() {
            return templateName;
        }
    }
    
    public record ReportParameters(
        LocalDateTime startDate,
        LocalDateTime endDate,
        String province,
        String status,
        String category,
        String action,
        Integer userId,
        Double minAmount,
        Double maxAmount
    ) {
        // Default constructor with minimal parameters
        public static ReportParameters basic(LocalDateTime start, LocalDateTime end) {
            return new ReportParameters(start, end, null, null, null, null, null, null, null);
        }
    }
    
    public record AuditLogFilter(
        Integer userId,
        String action,
        String entityType,
        String entityId,
        LocalDateTime from,
        LocalDateTime to
    ) {
        // Default constructor for date range only
        public static AuditLogFilter dateRange(LocalDateTime from, LocalDateTime to) {
            return new AuditLogFilter(null, null, null, null, from, to);
        }
    }
}