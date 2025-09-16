package com.orphanagehub.service;

import com.orphanagehub.dao.AuditLogDAO;
import com.orphanagehub.model.AuditLog;
import io.vavr.collection.List;
import io.vavr.control.Try;

public class AuditService {
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();
    
    public Try<Void> logAction(String action, Integer userId, String details) {
        return auditLogDAO.log(action, userId, details);
    }
    
    public Try<List<AuditLog>> getRecentAuditLogs(int limit) {
        return auditLogDAO.getRecentLogs(limit);
    }
}