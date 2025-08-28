/* Copyright (C) 2025 Jared Wisdom - All Rights Reserved */
package com.orphanagehub.dao;

import com.orphanagehub.model.Donation;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DonationDAO {
    public List<Donation> findByDonor(String donorId) throws SQLException {
        return new ArrayList<>();
    }
}
