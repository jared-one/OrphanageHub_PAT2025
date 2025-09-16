package com.orphanagehub.model;

import io.vavr.collection.List;
import io.vavr.control.Option;

public record OpportunitySearchCriteria(
    String category,
    List<String> skills,
    Integer maxHoursPerWeek,
    String city,
    String province,
    Integer volunteerAge,
    String sortBy
) {
    public static OpportunitySearchCriteria empty() {
        return new OpportunitySearchCriteria(
            null, List.empty(), null, null, null, null, null
        );
    }
}
