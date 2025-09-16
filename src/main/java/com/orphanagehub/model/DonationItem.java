package com.orphanagehub.model;

import io.vavr.control.Option;
import java.time.LocalDateTime;

/**
 * Represents individual items in a donation.
 */
public record DonationItem(
    Integer itemId,
    Integer donationId,
    String itemType,
    String itemDescription,
    Double quantity,
    Option<String> unit,
    Option<Double> estimatedValue,
    LocalDateTime createdDate
) {
    
    /**
     * Creates a new donation item.
     */
    public static DonationItem create(Integer donationId, String itemType, 
                                     String description, Double quantity, String unit) {
        return new DonationItem(
            null, donationId, itemType, description, quantity,
            Option.of(unit), Option.none(), LocalDateTime.now()
        );
    }
}