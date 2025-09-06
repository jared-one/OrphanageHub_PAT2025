package com.orphanagehub.model;

/**
 * Represents a ResourceRequest, as per Phase 2 UML.
 * Immutable with update method.
 */
public record ResourceRequest(String requestId, String orphanageId, String userId, String itemCategory,
                              String itemDescription, int quantityNeeded, int quantityFulfilled,
                              String urgency, String status, java.sql.Timestamp datePosted) {

    public static final String DEFAULT_STATUS = "Open"; // UML constant

    /**
     * Gets formatted request details.
     * @return Details string.
     */
    public String getRequestDetails() {
        return itemDescription + " (Needed: " + quantityNeeded + ", Fulfilled: " + quantityFulfilled + "), Status: " + status;
    }

    /**
     * Updates status immutably.
     * @param newStatus The new status.
     * @return New instance with updated status.
     */
    public ResourceRequest updateStatus(String newStatus) {
        return new ResourceRequest(requestId, orphanageId, userId, itemCategory, itemDescription,
                quantityNeeded, quantityFulfilled, urgency, newStatus, datePosted);
    }

    public ResourceRequest withQuantityFulfilled(int newQuantity) {
        return new ResourceRequest(requestId, orphanageId, userId, itemCategory, itemDescription,
                quantityNeeded, newQuantity, urgency, status, datePosted);
    }
}
