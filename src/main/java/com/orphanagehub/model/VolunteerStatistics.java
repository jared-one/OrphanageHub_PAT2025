package com.orphanagehub.model;

import io.vavr.collection.List;

public record VolunteerStatistics(
    int totalApplications,
    int acceptedApplications,
    int completedHours,
    int averageRating,
    List<String> categories
) {}

