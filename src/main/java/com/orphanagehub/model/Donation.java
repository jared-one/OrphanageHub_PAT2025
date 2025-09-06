package com.orphanagehub.model;

import io.vavr.control.Option;

/**
 * Represents a Donation (added for Phase 1 completeness).
 * Immutable.
 */
public record Donation(String donationId, String donorId, String orphanageId, String itemCategory,
                       Option<String> itemDescription, Option<Integer> amount, String status,
                       java.sql.Timestamp dateDonated) {

    /**
     * Gets formatted details.
     * @return Details string.
     */
    public String getDetails() {
        return "Donation to " + orphanageId + ": " + itemCategory + " (" + itemDescription.getOrElse("Monetary: " + amount.getOrElse(0)) + ")";
    }
}

