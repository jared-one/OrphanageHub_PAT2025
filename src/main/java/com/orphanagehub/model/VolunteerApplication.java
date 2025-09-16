package com.orphanagehub.model;

import io.vavr.control.Option;
import java.time.LocalDateTime;

/**
 * Immutable VolunteerApplication model representing the TblVolunteerApplications table.
 * Tracks volunteer applications to opportunities.
 * 
 * @author OrphanageHub Team
 * @version 2.0
 * @since 2025-09-06
 */
public record VolunteerApplication(
    Integer applicationId,
    Integer opportunityId,
    Integer volunteerId,
    LocalDateTime applicationDate,
    String status,
    Option<String> motivation,
    Option<String> experience,
    Option<String> availability,
    Option<LocalDateTime> interviewDate,
    Option<String> interviewNotes,
    Option<LocalDateTime> decisionDate,
    Option<Integer> decidedBy,
    Option<String> rejectionReason,
    Option<LocalDateTime> startDate,
    Option<LocalDateTime> endDate,
    Option<String> completionNotes,
    Option<Integer> hoursCompleted,
    Option<String> performanceRating,
    LocalDateTime createdDate,
    Option<LocalDateTime> modifiedDate,
    Option<Integer> modifiedBy
) {
    
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_REVIEWING = "Reviewing";
    public static final String STATUS_INTERVIEW_SCHEDULED = "Interview Scheduled";
    public static final String STATUS_ACCEPTED = "Accepted";
    public static final String STATUS_REJECTED = "Rejected";
    public static final String STATUS_WITHDRAWN = "Withdrawn";
    public static final String STATUS_ACTIVE = "Active";
    public static final String STATUS_COMPLETED = "Completed";
    
    /**
     * Creates a new application.
     */
    public static VolunteerApplication create(Integer opportunityId, Integer volunteerId,
                                             String motivation, String experience,
                                             String availability) {
        return new VolunteerApplication(
            null, opportunityId, volunteerId, LocalDateTime.now(), STATUS_PENDING,
            Option.of(motivation), Option.of(experience), Option.of(availability),
            Option.none(), Option.none(), Option.none(), Option.none(), Option.none(),
            Option.none(), Option.none(), Option.none(), Option.none(), Option.none(),
            LocalDateTime.now(), Option.none(), Option.none()
        );
    }
    
    /**
     * Checks if application is pending review.
     */
    public boolean isPending() {
        return STATUS_PENDING.equalsIgnoreCase(status) || 
               STATUS_REVIEWING.equalsIgnoreCase(status);
    }
    
    /**
     * Checks if application was successful.
     */
    public boolean isAccepted() {
        return STATUS_ACCEPTED.equalsIgnoreCase(status) || 
               STATUS_ACTIVE.equalsIgnoreCase(status) ||
               STATUS_COMPLETED.equalsIgnoreCase(status);
    }
    
    // Update methods
    public VolunteerApplication scheduleInterview(LocalDateTime interviewTime) {
        return new VolunteerApplication(
            applicationId, opportunityId, volunteerId, applicationDate,
            STATUS_INTERVIEW_SCHEDULED, motivation, experience, availability,
            Option.of(interviewTime), interviewNotes, decisionDate, decidedBy,
            rejectionReason, startDate, endDate, completionNotes, hoursCompleted,
            performanceRating, createdDate, Option.of(LocalDateTime.now()), modifiedBy
        );
    }
    
    public VolunteerApplication accept(Integer deciderId, LocalDateTime start) {
        return new VolunteerApplication(
            applicationId, opportunityId, volunteerId, applicationDate,
            STATUS_ACCEPTED, motivation, experience, availability, interviewDate,
            interviewNotes, Option.of(LocalDateTime.now()), Option.of(deciderId),
            Option.none(), Option.of(start), endDate, completionNotes,
            hoursCompleted, performanceRating, createdDate,
            Option.of(LocalDateTime.now()), Option.of(deciderId)
        );
    }
    
    public VolunteerApplication reject(Integer deciderId, String reason) {
        return new VolunteerApplication(
            applicationId, opportunityId, volunteerId, applicationDate,
            STATUS_REJECTED, motivation, experience, availability, interviewDate,
            interviewNotes, Option.of(LocalDateTime.now()), Option.of(deciderId),
            Option.of(reason), startDate, endDate, completionNotes, hoursCompleted,
            performanceRating, createdDate, Option.of(LocalDateTime.now()),
            Option.of(deciderId)
        );
    }
}