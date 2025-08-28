/* Copyright (C) 2025 Jared Wisdom - All Rights Reserved */
package com.orphanagehub.model;

import java.sql.Timestamp;

public class Donation {
    private String id;
    private String donorId;
    private String orphanageId;
    private double amount;
    private Timestamp timestamp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDonorId() {
        return donorId;
    }

    public void setDonorId(String donorId) {
        this.donorId = donorId;
    }

    public String getOrphanageId() {
        return orphanageId;
    }

    public void setOrphanageId(String orphanageId) {
        this.orphanageId = orphanageId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Timestamp getTimestamp() {
        return timestamp == null ? null : new Timestamp(timestamp.getTime());
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp == null ? null : new Timestamp(timestamp.getTime());
    }
}
