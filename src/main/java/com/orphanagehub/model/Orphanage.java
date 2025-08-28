/* Copyright (C) 2025 Jared Wisdom - All Rights Reserved */
package com.orphanagehub.model;

import java.time.LocalDateTime;

public class Orphanage {
    private String orphanageId;
    private String name;
    private String address;
    private String contactEmail;
    private String contactPhone;
    private String description;
    private int capacity;
    private int currentOccupancy;
    private LocalDateTime dateEstablished;
    private String status;

    public String getOrphanageId() {
        return orphanageId;
    }

    public void setOrphanageId(String orphanageId) {
        this.orphanageId = orphanageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getCurrentOccupancy() {
        return currentOccupancy;
    }

    public void setCurrentOccupancy(int currentOccupancy) {
        this.currentOccupancy = currentOccupancy;
    }

    public LocalDateTime getDateEstablished() {
        return dateEstablished;
    }

    public void setDateEstablished(LocalDateTime dateEstablished) {
        this.dateEstablished = dateEstablished;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
