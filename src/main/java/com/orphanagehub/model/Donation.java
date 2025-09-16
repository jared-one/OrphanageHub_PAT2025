package com.orphanagehub.model;

import io.vavr.control.Option;
import java.time.LocalDateTime;

/**
 * Immutable Donation model representing the TblDonations table.
 * Tracks monetary and item donations from donors to orphanages.
 * 
 * @author OrphanageHub Team
 * @version 2.0
 * @since 2025-09-06
 */
public record Donation(
    Integer donationId,
    Integer donorId,
    Integer orphanageId,
    Option<Integer> requestId,
    String donationType,
    Option<Double> amount,
    String currency,
    Option<String> itemDescription,
    Option<Double> quantity,
    Option<String> unit,
    Option<Double> estimatedValue,
    LocalDateTime donationDate,
    Option<LocalDateTime> scheduledDate,
    String status,
    Option<String> paymentMethod,
    Option<String> transactionReference,
    boolean taxDeductible,
    boolean anonymousDonation,
    boolean recurringDonation,
    Option<String> recurrenceInterval,
    Option<LocalDateTime> nextRecurrenceDate,
    Option<String> donorMessage,
    boolean thankYouSent,
    Option<LocalDateTime> thankYouDate,
    Option<String> receiptNumber,
    boolean receiptSent,
    Option<String> notes,
    LocalDateTime createdDate,
    Option<LocalDateTime> modifiedDate,
    Option<Integer> modifiedBy
) {
    
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_PROCESSING = "Processing";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_FAILED = "Failed";
    public static final String STATUS_CANCELLED = "Cancelled";
    
    public static final String TYPE_MONEY = "Money";
    public static final String TYPE_FOOD = "Food";
    public static final String TYPE_CLOTHING = "Clothing";
    public static final String TYPE_EDUCATIONAL = "Educational";
    public static final String TYPE_MEDICAL = "Medical";
    public static final String TYPE_OTHER = "Other";
    
    /**
     * Creates a monetary donation.
     */
    public static Donation createMonetary(Integer donorId, Integer orphanageId,
                                         Double amount, String paymentMethod) {
        return new Donation(
            null, donorId, orphanageId, Option.none(), TYPE_MONEY,
            Option.of(amount), "ZAR", Option.none(), Option.none(), Option.none(),
            Option.of(amount), LocalDateTime.now(), Option.none(), STATUS_PENDING,
            Option.of(paymentMethod), Option.none(), true, false, false,
            Option.none(), Option.none(), Option.none(), false, Option.none(),
            Option.none(), false, Option.none(), LocalDateTime.now(),
            Option.none(), Option.none()
        );
    }
    
    /**
     * Creates an item donation.
     */
    public static Donation createItem(Integer donorId, Integer orphanageId,
                                     String itemType, String description,
                                     Double qty, String unit) {
        return new Donation(
            null, donorId, orphanageId, Option.none(), itemType,
            Option.none(), "ZAR", Option.of(description), Option.of(qty),
            Option.of(unit), Option.none(), LocalDateTime.now(), Option.none(),
            STATUS_PENDING, Option.none(), Option.none(), true, false, false,
            Option.none(), Option.none(), Option.none(), false, Option.none(),
            Option.none(), false, Option.none(), LocalDateTime.now(),
            Option.none(), Option.none()
        );
    }
    
    /**
     * Gets donation details as formatted string.
     */
    public String getDetails() {
        StringBuilder details = new StringBuilder();
        
        if (TYPE_MONEY.equals(donationType)) {
            details.append("Monetary donation: ");
            amount.forEach(amt -> details.append(currency).append(" ").append(String.format("%.2f", amt)));
        } else {
            details.append(donationType).append(" donation: ");
            itemDescription.forEach(desc -> details.append(desc).append(" "));
            quantity.forEach(qty -> {
                details.append("(").append(qty);
                unit.forEach(u -> details.append(" ").append(u));
                details.append(")");
            });
        }
        
        details.append(" - Status: ").append(status);
        
        if (anonymousDonation) {
            details.append(" [Anonymous]");
        }
        
        return details.toString();
    }
    
    /**
     * Gets the total value of the donation.
     */
    public Double getTotalValue() {
        if (TYPE_MONEY.equals(donationType)) {
            return amount.getOrElse(0.0);
        }
        return estimatedValue.getOrElse(0.0);
    }
    
    /**
     * Checks if donation is complete.
     */
    public boolean isComplete() {
        return STATUS_COMPLETED.equalsIgnoreCase(status);
    }
    
    /**
     * Checks if donation needs processing.
     */
    public boolean needsProcessing() {
        return STATUS_PENDING.equalsIgnoreCase(status) || 
               STATUS_PROCESSING.equalsIgnoreCase(status);
    }
    
    // Immutable update methods
    public Donation withStatus(String newStatus) {
        return new Donation(
            donationId, donorId, orphanageId, requestId, donationType, amount,
            currency, itemDescription, quantity, unit, estimatedValue, donationDate,
            scheduledDate, newStatus, paymentMethod, transactionReference,
            taxDeductible, anonymousDonation, recurringDonation, recurrenceInterval,
            nextRecurrenceDate, donorMessage, thankYouSent, thankYouDate,
            receiptNumber, receiptSent, notes, createdDate,
            Option.of(LocalDateTime.now()), modifiedBy
        );
    }
    
    public Donation markCompleted(String transactionRef) {
        return new Donation(
            donationId, donorId, orphanageId, requestId, donationType, amount,
            currency, itemDescription, quantity, unit, estimatedValue, donationDate,
            scheduledDate, STATUS_COMPLETED, paymentMethod,
            Option.of(transactionRef), taxDeductible, anonymousDonation,
            recurringDonation, recurrenceInterval, nextRecurrenceDate, donorMessage,
            thankYouSent, thankYouDate, receiptNumber, receiptSent, notes,
            createdDate, Option.of(LocalDateTime.now()), modifiedBy
        );
    }
    
    public Donation markThankYouSent(String receipt) {
        return new Donation(
            donationId, donorId, orphanageId, requestId, donationType, amount,
            currency, itemDescription, quantity, unit, estimatedValue, donationDate,
            scheduledDate, status, paymentMethod, transactionReference,
            taxDeductible, anonymousDonation, recurringDonation, recurrenceInterval,
            nextRecurrenceDate, donorMessage, true, Option.of(LocalDateTime.now()),
            Option.of(receipt), true, notes, createdDate,
            Option.of(LocalDateTime.now()), modifiedBy
        );
    }
}