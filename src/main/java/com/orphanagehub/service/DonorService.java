/* Copyright (C) 2025 Jared Wisdom - All Rights Reserved */
package com.orphanagehub.service;

import com.orphanagehub.dao.DonationDAO;
import com.orphanagehub.model.Donation;
import java.sql.SQLException;
import java.util.List;

public class DonorService {
    private final DonationDAO donationDAO = new DonationDAO();

    public List<Donation> getDonationsForDonor(String donorId) throws ServiceException {
        try {
            return donationDAO.findByDonor(donorId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to load donations", e);
        }
    }
}
