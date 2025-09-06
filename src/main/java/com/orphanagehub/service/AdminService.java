package com.orphanagehub.service;

import com.orphanagehub.dao.OrphanageDAO;
import com.orphanagehub.dao.UserDAO;
import com.orphanagehub.dao.DatabaseManager;
import com.orphanagehub.model.Orphanage;
import com.orphanagehub.model.User;
import io.vavr.collection.List;
import io.vavr.control.Try;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

import java.util.HashMap;

/**
 * Service for admin operations.
 * Handles user management, verifications, and reports.
 */
public class AdminService {

    private final UserDAO userDAO = new UserDAO();
    private final OrphanageDAO orphanageDAO = new OrphanageDAO();

    /**
     * Manages users (suspend/activate).
     * @param user The User to manage.
     * @param active New status (true/false).
     * @return Try<Void> - success on update, failure on error.
     */
    public Try<Void> manageUser(User user, boolean active) {
        // Assuming User record has withAccountStatus method or similar; adjust if needed
        User updatedUser = user.withAccountStatus(active ? "Active" : "Suspended");
        return userDAO.update(updatedUser);
    }

    /**
     * Verifies an orphanage.
     * @param orphanageId The ID to verify.
     * @param status New verification status.
     * @return Try<Void> - success on update, failure on error.
     */
    public Try<Void> verifyOrphanage(String orphanageId, String status) {
        return orphanageDAO.findById(orphanageId)
                .flatMap(opt -> opt.toTry(() -> new IllegalArgumentException("Orphanage not found")))
                .<Orphanage>map(o -> o.withVerificationStatus(status))
                .flatMap(orphanageDAO::update).map(o -> (Void) null);
    }

    /**
     * Generates a system report using Jasper.
     * @param reportType The type (e.g., "users").
     * @return Try<String> - path to exported PDF on success.
     */
    public Try<String> generateReport(String reportType) {
        return DatabaseManager.getConnection()
            .flatMap(conn -> Try.of(() -> {
                HashMap<String, Object> params = new HashMap<>(); // Dynamic params
                JasperPrint print = JasperFillManager.fillReport("reports/" + reportType + ".jasper", params, conn);
                String path = "reports/" + reportType + "_report.pdf";
                JasperExportManager.exportReportToPdfFile(print, path);
                return path;
            }));
    }

    // Additional: Get pending verifications
    public Try<List<Orphanage>> getPendingVerifications() {
        return orphanageDAO.findByVerificationStatus("Pending").map(List::ofAll);
    }
}