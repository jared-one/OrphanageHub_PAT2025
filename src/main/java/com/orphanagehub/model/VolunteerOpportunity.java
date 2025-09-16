package com.orphanagehub.model;

import io.vavr.control.Option;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Immutable VolunteerOpportunity model representing the TblVolunteerOpportunities table.
 */
public record VolunteerOpportunity(
    Integer opportunityId,
    Integer orphanageId,
    String title,
    String description,
    String category,
    Option<String> skillsRequired,
    Option<String> skillLevel,
    Option<String> timeCommitment,
    Option<Integer> hoursPerWeek,
    Option<String> duration,
    Option<LocalDate> startDate,
    Option<LocalDate> endDate,
    Option<String> recurringSchedule,
    Option<Integer> minAge,
    Option<Integer> maxAge,
    Option<Integer> maxVolunteers,
    Integer currentVolunteers,
    boolean backgroundCheckRequired,
    boolean trainingProvided,
    Option<String> trainingDetails,
    boolean transportProvided,
    boolean mealsProvided,
    String status,
    String urgencyLevel,
    LocalDateTime createdDate,
    Integer createdBy,
    Option<LocalDateTime> modifiedDate,
    Option<Integer> modifiedBy,
    Option<LocalDateTime> publishedDate,
    Option<LocalDateTime> closedDate,
    Option<String> location
) {
    
    public static final String STATUS_OPEN = "Open";
    public static final String STATUS_CLOSED = "Closed";
    
    public static final String URGENCY_LOW = "Low";
    public static final String URGENCY_MEDIUM = "Medium";
    public static final String URGENCY_HIGH = "High";
    public static final String URGENCY_CRITICAL = "Critical";
    
    /**
     * Factory method to create a basic volunteer opportunity with minimal required fields
     */
    public static VolunteerOpportunity createBasic(
            Integer orphanageId,
            String title,
            String description,
            String category,
            int createdBy) {
        return new VolunteerOpportunity(
            null, // opportunityId will be assigned by database
            orphanageId,
            title,
            description,
            category,
            Option.none(), // skillsRequired
            Option.none(), // skillLevel
            Option.none(), // timeCommitment
            Option.none(), // hoursPerWeek
            Option.none(), // duration
            Option.none(), // startDate
            Option.none(), // endDate
            Option.none(), // recurringSchedule
            Option.none(), // minAge
            Option.none(), // maxAge
            Option.of(10), // maxVolunteers - default to 10
            0, // currentVolunteers - starts at 0
            false, // backgroundCheckRequired
            false, // trainingProvided
            Option.none(), // trainingDetails
            false, // transportProvided
            false, // mealsProvided
            STATUS_OPEN, // status - default to Open
            URGENCY_MEDIUM, // urgencyLevel - default to Medium
            LocalDateTime.now(), // createdDate
            createdBy,
            Option.none(), // modifiedDate
            Option.none(), // modifiedBy
            Option.of(LocalDateTime.now()), // publishedDate - publish immediately
            Option.none(), // closedDate
            Option.none() // location
        );
    }
    
    /**
     * Factory method for creating a volunteer opportunity with all details
     */
    public static VolunteerOpportunity createDetailed(
            Integer orphanageId,
            String title,
            String description,
            String category,
            String skillsRequired,
            String skillLevel,
            String timeCommitment,
            Integer hoursPerWeek,
            String urgencyLevel,
            int createdBy) {
        return new VolunteerOpportunity(
            null,
            orphanageId,
            title,
            description,
            category,
            Option.of(skillsRequired),
            Option.of(skillLevel),
            Option.of(timeCommitment),
            Option.of(hoursPerWeek),
            Option.none(),
            Option.none(),
            Option.none(),
            Option.none(),
            Option.none(),
            Option.none(),
            Option.of(10),
            0,
            false,
            false,
            Option.none(),
            false,
            false,
            STATUS_OPEN,
            urgencyLevel,
            LocalDateTime.now(),
            createdBy,
            Option.none(),
            Option.none(),
            Option.of(LocalDateTime.now()),
            Option.none(),
            Option.none()
        );
    }
    
    public boolean isOpen() {
        return STATUS_OPEN.equalsIgnoreCase(status);
    }
    
    public boolean isAgeEligible(int age) {
        boolean aboveMin = minAge.map(min -> age >= min).getOrElse(true);
        boolean belowMax = maxAge.map(max -> age <= max).getOrElse(true);
        return aboveMin && belowMax;
    }

    public Integer slotsAvailable() {
        return maxVolunteers.getOrElse(0) - currentVolunteers;
    }

    public Integer applicationCount() {
        return 0; // Placeholder - would be calculated from applications
    }
    
    // Backward compatibility methods
    public String skillRequired() {
        return skillsRequired.getOrElse("");
    }
    
    /**
     * Creates a copy with updated status
     */
    public VolunteerOpportunity withStatus(String newStatus) {
        return new VolunteerOpportunity(
            opportunityId, orphanageId, title, description, category,
            skillsRequired, skillLevel, timeCommitment, hoursPerWeek,
            duration, startDate, endDate, recurringSchedule, minAge,
            maxAge, maxVolunteers, currentVolunteers, backgroundCheckRequired,
            trainingProvided, trainingDetails, transportProvided, mealsProvided,
            newStatus, urgencyLevel, createdDate, createdBy, 
            Option.of(LocalDateTime.now()), Option.of(createdBy),
            publishedDate, 
            STATUS_CLOSED.equals(newStatus) ? Option.of(LocalDateTime.now()) : closedDate,
            location
        );
    }
    
    /**
     * Creates a copy with incremented volunteer count
     */
    public VolunteerOpportunity incrementVolunteers() {
        return new VolunteerOpportunity(
            opportunityId, orphanageId, title, description, category,
            skillsRequired, skillLevel, timeCommitment, hoursPerWeek,
            duration, startDate, endDate, recurringSchedule, minAge,
            maxAge, maxVolunteers, currentVolunteers + 1, backgroundCheckRequired,
            trainingProvided, trainingDetails, transportProvided, mealsProvided,
            status, urgencyLevel, createdDate, createdBy, 
            Option.of(LocalDateTime.now()), Option.of(createdBy),
            publishedDate, closedDate, location
        );
    }
}