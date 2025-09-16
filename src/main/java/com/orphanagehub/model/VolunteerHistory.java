package com.orphanagehub.model;

import io.vavr.control.Option;
import java.time.LocalDate;

public record VolunteerHistory(
    LocalDate startDate,
    Option<LocalDate> endDate,
    String opportunityTitle,
    String orphanageName,
    int hoursCompleted,
    String roleName,
    String status,
    boolean certificateAvailable
) {
    public static VolunteerHistory create(LocalDate startDate, String title, String orphanage, int hours) {
        return new VolunteerHistory(
            startDate,
            Option.none(),
            title,
            orphanage,
            hours,
            title,
            "Active",
            false
        );
    }
}
