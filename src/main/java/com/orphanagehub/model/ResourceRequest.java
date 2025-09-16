package com.orphanagehub.model;

import io.vavr.control.Option;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Immutable ResourceRequest model representing the TblResourceRequests table.
 * Includes all fields from the expanded database schema.
 * 
 * @author OrphanageHub Team
 * @version 2.0
 * @since 2025-09-06
 */
public record ResourceRequest(
    Integer requestId,
    Integer orphanageId,
    String resourceType,
    String resourceDescription,
    Double quantity,
    Option<String> unit,
    String urgencyLevel,
    LocalDateTime requestDate,
    Option<LocalDate> neededByDate,
    String status,
    Option<LocalDateTime> fulfilledDate,
    Option<Integer> fulfilledBy,
    Option<String> fulfillmentNotes,
    Option<Double> estimatedValue,
    Option<Double> actualValue,
    Option<String> notes,
    Option<String> imagePath,
    Integer createdBy,
    Option<LocalDateTime> modifiedDate,
    Option<Integer> modifiedBy
) {
    
    public static final String DEFAULT_STATUS = "Open";
    public static final String STATUS_OPEN = "Open";
    public static final String STATUS_IN_PROGRESS = "In Progress";
    public static final String STATUS_FULFILLED = "Fulfilled";
    public static final String STATUS_CANCELLED = "Cancelled";
    
    /**
     * Creates a ResourceRequest with minimal required fields.
     */
    public static ResourceRequest createBasic(Integer orphanageId, String resourceType,
                                             String resourceDescription, Double quantity,
                                             String urgencyLevel, Integer createdBy) {
        return new ResourceRequest(
            null, orphanageId, resourceType, resourceDescription, quantity,
            Option.none(), urgencyLevel, LocalDateTime.now(), Option.none(),
            DEFAULT_STATUS, Option.none(), Option.none(), Option.none(),
            Option.none(), Option.none(), Option.none(), Option.none(),
            createdBy, Option.none(), Option.none()
        );
    }
    
    /**
     * Gets formatted request details.
     */
    public String getRequestDetails() {
        StringBuilder details = new StringBuilder();
        details.append(resourceDescription);
        details.append(" (Qty: ").append(quantity);
        unit.forEach(u -> details.append(" ").append(u));
        details.append(", Urgency: ").append(urgencyLevel);
        details.append(", Status: ").append(status);
        estimatedValue.forEach(val -> details.append(", Est. Value: R").append(String.format("%.2f", val)));
        details.append(")");
        return details.toString();
    }
    
    /**
     * Checks if request is urgent (Critical or High).
     */
    public boolean isUrgent() {
        return "Critical".equalsIgnoreCase(urgencyLevel) || 
               "High".equalsIgnoreCase(urgencyLevel);
    }
    
    /**
     * Checks if request is open for fulfillment.
     */
    public boolean isOpen() {
        return STATUS_OPEN.equalsIgnoreCase(status);
    }
    
    /**
     * Checks if request is overdue.
     */
    public boolean isOverdue() {
        return neededByDate.map(date -> LocalDate.now().isAfter(date) && isOpen()).getOrElse(false);
    }
    
    /**
     * Gets days until needed (negative if overdue).
     */
    public Option<Long> getDaysUntilNeeded() {
        return neededByDate.map(date -> {
            long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), date);
            return days;
        });
    }
    
    /**
     * Gets fulfillment percentage (if actualValue is available).
     */
    public Option<Double> getFulfillmentPercentage() {
        if (estimatedValue.isEmpty() || actualValue.isEmpty() || estimatedValue.get() == 0) {
            return Option.none();
        }
        return Option.of((actualValue.get() / estimatedValue.get()) * 100);
    }
    
    // Immutable update methods
    public ResourceRequest updateStatus(String newStatus) {
        return new ResourceRequest(
            requestId, orphanageId, resourceType, resourceDescription, quantity,
            unit, urgencyLevel, requestDate, neededByDate, newStatus, fulfilledDate,
            fulfilledBy, fulfillmentNotes, estimatedValue, actualValue, notes,
            imagePath, createdBy, Option.of(LocalDateTime.now()), modifiedBy
        );
    }
    
    public ResourceRequest markFulfilled(Integer fulfilledById, String notes, Double actualVal) {
        return new ResourceRequest(
            requestId, orphanageId, resourceType, resourceDescription, quantity,
            unit, urgencyLevel, requestDate, neededByDate, STATUS_FULFILLED,
            Option.of(LocalDateTime.now()), Option.of(fulfilledById),
            Option.of(notes), estimatedValue, Option.of(actualVal), this.notes,
            imagePath, createdBy, Option.of(LocalDateTime.now()), Option.of(fulfilledById)
        );
    }
    
    public ResourceRequest withEstimatedValue(Double value) {
        return new ResourceRequest(
            requestId, orphanageId, resourceType, resourceDescription, quantity,
            unit, urgencyLevel, requestDate, neededByDate, status, fulfilledDate,
            fulfilledBy, fulfillmentNotes, Option.of(value), actualValue, notes,
            imagePath, createdBy, Option.of(LocalDateTime.now()), modifiedBy
        );
    }
public ResourceRequest withActualValue(Double value) {
    return new ResourceRequest(
        requestId, orphanageId, resourceType, resourceDescription, quantity,
        unit, urgencyLevel, requestDate, neededByDate, status, fulfilledDate,
        fulfilledBy, fulfillmentNotes, estimatedValue, Option.of(value), notes,
        imagePath, createdBy, Option.of(LocalDateTime.now()), modifiedBy
    );
}
    
    // Added compatibility methods for services
    
    /**
     * Alias for quantity for backwards compatibility
     */
    public Double quantityNeeded() {
        return quantity;
    }
    
    /**
     * Quantity fulfilled calculation (based on status)
     */
    public Double quantityFulfilled() {
        if (STATUS_FULFILLED.equalsIgnoreCase(status)) {
            return quantity;
        } else if (STATUS_IN_PROGRESS.equalsIgnoreCase(status)) {
            // Could track partial fulfillment if needed
            return 0.0;
        }
        return 0.0;
    }
    
    /**
     * Updates with quantity fulfilled
     */
    public ResourceRequest withQuantityFulfilled(Double fulfilled) {
        String newStatus = fulfilled >= quantity ? STATUS_FULFILLED : STATUS_IN_PROGRESS;
        return updateStatus(newStatus);
    }
}