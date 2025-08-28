/* Copyright (C) 2025 Jared Wisdom - All Rights Reserved */
package com.orphanagehub.service;

import com.orphanagehub.dao.OrphanageDAO;
import com.orphanagehub.model.Orphanage;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrphanageService {
    private static final Logger logger = LoggerFactory.getLogger(OrphanageService.class);
    private final OrphanageDAO orphanageDAO = new OrphanageDAO();

    public Orphanage getOrphanageForStaff(String userId) throws ServiceException {
        try {
            Orphanage orphanage = orphanageDAO.findByStaffUserId(userId);
            if (orphanage == null) {
                throw new ServiceException("No orphanage associated with this staff user.");
            }
            return orphanage;
        } catch (SQLException e) {
            logger.error("Database error fetching orphanage for staff", e);
            throw new ServiceException("Failed to retrieve orphanage information.");
        }
    }
}
