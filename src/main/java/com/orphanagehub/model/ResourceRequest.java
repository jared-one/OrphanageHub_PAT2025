// src/main/java/com/orphanagehub/model/ResourceRequest.java
/* Copyright (C) 2025 Jared Wisdom - All Rights Reserved */
package com.orphanagehub.model;

public class ResourceRequest {
    private String id;
    private String orphanageId;
    private String category;
    private String description;
    private int needed;
    private int fulfilled;
    private String urgency;
    private String status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrphanageId() {
        return orphanageId;
    }

    public void setOrphanageId(String orphanageId) {
        this.orphanageId = orphanageId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getNeeded() {
        return needed;
    }

    public void setNeeded(int needed) {
        this.needed = needed;
    }

    public int getFulfilled() {
        return fulfilled;
    }

    public void setFulfilled(int fulfilled) {
        this.fulfilled = fulfilled;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}