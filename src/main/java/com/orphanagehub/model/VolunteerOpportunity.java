package com.orphanagehub.model;

import io.vavr.control.Option;

/**
 * Represents a VolunteerOpportunity (added for Phase 1 completeness).
 * Immutable.
 */
public record VolunteerOpportunity(String opportunityId, String orphanageId, String skillRequired,
                                   String location, Option<String> timeCommitment, String status) {

    /**
     * Gets formatted details.
     * @return Details string.
     */
    public String getDetails() {
        return "Opportunity at " + orphanageId + ": " + skillRequired + " in " + location;
    }
}
