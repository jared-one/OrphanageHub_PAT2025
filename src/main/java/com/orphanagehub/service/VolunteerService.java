package com.orphanagehub.service;

import com.orphanagehub.dao.*;
import com.orphanagehub.model.*;
import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Service for volunteer operations.
 * 
 * @author OrphanageHub Team
 * @version 2.0
 * @since 2025-09-06
 */
public class VolunteerService {
    private static final Logger logger = LoggerFactory.getLogger(VolunteerService.class);
    
    private final VolunteerOpportunityDAO opportunityDAO = new VolunteerOpportunityDAO();
    private final VolunteerApplicationDAO applicationDAO = new VolunteerApplicationDAO();
    private final UserDAO userDAO = new UserDAO();
    private final OrphanageDAO orphanageDAO = new OrphanageDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();
    
    /**
     * Searches volunteer opportunities
     */
    public Try<List<VolunteerOpportunity>> searchOpportunities(OpportunitySearchCriteria criteria) {
        return opportunityDAO.findOpenOpportunities()
            .map(opportunities -> {
                List<VolunteerOpportunity> filtered = opportunities;
                
                // Filter by category
                if (criteria.category() != null) {
                    filtered = filtered.filter(o -> o.category().equals(criteria.category()));
                }
                
                // Filter by skills
                if (criteria.skills() != null && !criteria.skills().isEmpty()) {
                    filtered = filtered.filter(o -> 
                        o.skillsRequired().map(skills ->
                            criteria.skills().exists(skill ->
                                skills.toLowerCase().contains(skill.toLowerCase())
                            )
                        ).getOrElse(true)
                    );
                }
                
                // Filter by time commitment
                if (criteria.maxHoursPerWeek() != null) {
                    filtered = filtered.filter(o ->
                        o.hoursPerWeek().map(hours -> 
                            hours <= criteria.maxHoursPerWeek()
                        ).getOrElse(true)
                    );
                }
                
                // Filter by location
                if (criteria.city() != null || criteria.province() != null) {
                    filtered = filterByLocation(filtered, criteria.city(), criteria.province());
                }
                
                // Filter by age eligibility
                if (criteria.volunteerAge() != null) {
                    filtered = filtered.filter(o -> o.isAgeEligible(criteria.volunteerAge()));
                }
                
                // Sort
                if (criteria.sortBy() != null) {
                    filtered = sortOpportunities(filtered, criteria.sortBy());
                }
                
                return filtered;
            });
    }
    
    public Try<List<VolunteerOpportunity>> getOpportunities(
            Option<String> locationFilter, 
            Option<String> skillsFilter, 
            Option<String> timeFilter) {
        
        return Try.of(() -> {
            // Build SQL query dynamically
            StringBuilder sql = new StringBuilder(
                "SELECT vo.OpportunityID, vo.OrphanageID, vo.Title, vo.Description, " +
                "vo.Category, vo.RequiredSkills, vo.TimeCommitment, vo.Location, " +
                "vo.Capacity, vo.CurrentVolunteers, vo.Status, vo.PostedDate, vo.PostedBy, " +
                "o.OrphanageName " +
                "FROM TblVolunteerOpportunities vo " +
                "JOIN TblOrphanages o ON vo.OrphanageID = o.OrphanageID " +
                "WHERE vo.Status = 'Open'"
            );
            
            // Build parameters list
            List<Object> params = List.empty();
            
            // Add location filter if present
            if (locationFilter.isDefined()) {
                sql.append(" AND vo.Location LIKE ?");
                params = params.append("%" + locationFilter.get() + "%");
            }
            
            // Add skills filter if present
            if (skillsFilter.isDefined()) {
                sql.append(" AND vo.RequiredSkills LIKE ?");
                params = params.append("%" + skillsFilter.get() + "%");
            }
            
            // Add time commitment filter if present
            if (timeFilter.isDefined()) {
                sql.append(" AND vo.TimeCommitment = ?");
                params = params.append(timeFilter.get());
            }
            
            sql.append(" ORDER BY vo.PostedDate DESC");
            
            // Execute query using DAO
            return opportunityDAO.executeQuery(sql.toString(), params, rs -> {
                try {
                    // Map ResultSet to VolunteerOpportunity
                    // Using simplified mapping for basic table structure
                    return new VolunteerOpportunity(
                        rs.getInt("OpportunityID"),
                        rs.getInt("OrphanageID"),
                        rs.getString("Title"),
                        rs.getString("Description"),
                        rs.getString("Category"),
                        Option.of(rs.getString("RequiredSkills")),
                        Option.none(), // SkillLevel - not in basic table
                        Option.of(rs.getString("TimeCommitment")),
                        Option.none(), // HoursPerWeek - optional
                        Option.none(), // Duration - optional
                        Option.none(), // StartDate - optional
                        Option.none(), // EndDate - optional
                        Option.none(), // RecurringSchedule - optional
                        Option.none(), // MinAge - optional
                        Option.none(), // MaxAge - optional
                        Option.of(rs.getInt("Capacity")),
                        rs.getInt("CurrentVolunteers"),
                        false, // BackgroundCheckRequired - default
                        false, // TrainingProvided - default
                        Option.none(), // TrainingDetails
                        false, // TransportProvided - default
                        false, // MealsProvided - default
                        rs.getString("Status"),
                        "Normal", // UrgencyLevel - default
                        rs.getTimestamp("PostedDate").toLocalDateTime(),
                        rs.getInt("PostedBy"),
                        Option.none(), // ModifiedDate
                        Option.none(), // ModifiedBy
                        Option.none(), // PublishedDate
                        Option.none(), // ClosedDate
                        Option.of(rs.getString("Location"))
                    );
                } catch (SQLException e) {
                    throw new RuntimeException("Failed to map VolunteerOpportunity from ResultSet", e);
                }
            });
        }).flatMap(result -> result); // Flatten Try<Try<List>> to Try<List>
    }
    
    /**
     * Applies for volunteer opportunity
     */
    public Try<VolunteerApplication> applyForOpportunity(
            Integer opportunityId, Integer volunteerId,
            ApplicationDetails details) {
        
        // Check if already applied
        return applicationDAO.hasApplied(volunteerId, opportunityId)
            .flatMap(hasApplied -> {
                if (hasApplied) {
                    return Try.failure(new ServiceException("You have already applied for this opportunity"));
                }
                
                // Get opportunity details
                return opportunityDAO.findById(opportunityId)
                    .flatMap(oppOpt -> oppOpt.toTry(() -> 
                        new ServiceException("Opportunity not found")))
                    .flatMap(opportunity -> {
                        // Check if opportunity is open
                        if (!opportunity.isOpen()) {
                            return Try.failure(new ServiceException("This opportunity is no longer available"));
                        }
                        
                        // Check age eligibility
                        userDAO.findById(volunteerId).forEach(userOpt -> {
                            userOpt.forEach(user -> {
                                user.dateOfBirth().forEach(dob -> {
                                    int age = java.time.Period.between(dob, java.time.LocalDate.now()).getYears();
                                    if (!opportunity.isAgeEligible(age)) {
                                        throw new RuntimeException("You do not meet the age requirements");
                                    }
                                });
                            });
                        });
                        
                        // Create application
                        VolunteerApplication application = VolunteerApplication.create(
                            opportunityId,
                            volunteerId,
                            details.motivation(),
                            details.experience(),
                            details.availability()
                        );
                        
                        return applicationDAO.create(application);
                    })
                    .flatMap(application -> {
                        // Notify orphanage
                        notifyOrphanageAboutApplication(application);
                        
                        // Log application
                        auditLogDAO.logSuccess(volunteerId, null, AuditLog.ACTION_APPLY,
                            "VolunteerOpportunity", opportunityId.toString());
                        
                        logger.info("Volunteer {} applied for opportunity {}", 
                            volunteerId, opportunityId);
                        
                        return Try.success(application);
                    });
            });
    }
    
    /**
     * Gets volunteer's applications
     */
    public Try<List<VolunteerApplication>> getMyApplications(Integer volunteerId) {
        return applicationDAO.findByVolunteerId(volunteerId);
    }
    
    /**
     * Withdraws application
     */
    public Try<Void> withdrawApplication(Integer applicationId, Integer volunteerId) {
        return applicationDAO.findById(applicationId)
            .flatMap(appOpt -> appOpt.toTry(() -> 
                new ServiceException("Application not found")))
            .flatMap(application -> {
                // Verify ownership
                if (!application.volunteerId().equals(volunteerId)) {
                    return Try.failure(new ServiceException("Unauthorized"));
                }
                
                // Check if can be withdrawn
                if (application.isAccepted()) {
                    return Try.failure(new ServiceException("Cannot withdraw accepted application"));
                }
                
                return applicationDAO.updateStatus(applicationId, "Withdrawn", volunteerId)
                    .flatMap(v -> {
                        // Decrement opportunity volunteer count
                        opportunityDAO.decrementVolunteers(application.opportunityId());
                        
                        // Log withdrawal
                        auditLogDAO.logSuccess(volunteerId, null, AuditLog.ACTION_UPDATE,
                            "VolunteerApplication", "Withdrawn: " + applicationId);
                        
                        logger.info("Application {} withdrawn", applicationId);
                        return Try.success(null);
                    });
            });
    }
    
    /**
     * Gets volunteer statistics
     */
    public Try<VolunteerStatistics> getVolunteerStatistics(Integer volunteerId) {
        return applicationDAO.findByVolunteerId(volunteerId)
            .map(applications -> {
                int totalApplications = applications.size();
                int acceptedApplications = applications.filter(VolunteerApplication::isAccepted).size();
                int pendingApplications = applications.filter(VolunteerApplication::isPending).size();
                
                int totalHours = applications
                    .flatMap(app -> app.hoursCompleted())
                    .sum().intValue();
                
                List<String> categories = applications
                    .flatMap(app -> opportunityDAO.findById(app.opportunityId())
                        .getOrElse(Option.none())
                        .map(VolunteerOpportunity::category))
                    .distinct();
                
                return new VolunteerStatistics(
                    totalApplications,
                    acceptedApplications,
                    pendingApplications,
                    totalHours,
                    categories.size(),
                    categories
                );
            });
    }

    public Try<Option<VolunteerOpportunity>> getOpportunityById(Integer opportunityId) {
        return opportunityDAO.findById(opportunityId);
    }

    public Try<List<VolunteerApplication>> getVolunteerHistory(Integer volunteerId) {
        return applicationDAO.findByVolunteerId(volunteerId);
    }

    public Try<List<VolunteerApplication>> getUpcomingEvents(Integer volunteerId) {
        return applicationDAO.findByVolunteerId(volunteerId)
            .map(apps -> apps.filter(app ->
                "Accepted".equalsIgnoreCase(app.status()) &&
                app.startDate().isDefined() &&
                app.startDate().get().isAfter(LocalDateTime.now())
            ));
    }

    public Try<List<VolunteerOpportunity>> getOpportunitiesForOrphanage(String orphanageId) {
        return Try.of(() -> Integer.valueOf(orphanageId))
            .flatMap(id -> opportunityDAO.findByOrphanageId(id));
    }
    
    // Helper methods
    
    private List<VolunteerOpportunity> filterByLocation(
            List<VolunteerOpportunity> opportunities, 
            String city, String province) {
        
        return opportunities.filter(opp -> {
            // Get orphanage location
            Option<Orphanage> orphanage = orphanageDAO.findById(opp.orphanageId())
                .getOrElse(Option.none());
            
            return orphanage.map(orph -> {
                boolean cityMatch = city == null || orph.city().equals(city);
                boolean provinceMatch = province == null || orph.province().equals(province);
                return cityMatch && provinceMatch;
            }).getOrElse(false);
        });
    }
    
    private List<VolunteerOpportunity> sortOpportunities(
            List<VolunteerOpportunity> opportunities, String sortBy) {
        
        return switch (sortBy.toLowerCase()) {
            case "newest" -> opportunities.sortBy(VolunteerOpportunity::createdDate).reverse();
            case "urgent" -> opportunities.sortBy(o -> 
                o.urgencyLevel().equals("High") ? 0 : 1);
            case "category" -> opportunities.sortBy(VolunteerOpportunity::category);
            case "hours" -> opportunities.sortBy(o -> 
                o.hoursPerWeek().getOrElse(0));
            default -> opportunities;
        };
    }
    
    private void notifyOrphanageAboutApplication(VolunteerApplication application) {
        opportunityDAO.findById(application.opportunityId()).forEach(oppOpt -> {
            oppOpt.forEach(opportunity -> {
                orphanageDAO.findById(opportunity.orphanageId()).forEach(orphOpt -> {
                    orphOpt.forEach(orphanage -> {
                        userDAO.findById(orphanage.userId()).forEach(userOpt -> {
                            userOpt.forEach(user -> {
                                Notification notification = new Notification(
                                    null,
                                    user.userId(),
                                    Notification.TYPE_VOLUNTEER,
                                    "New Volunteer Application",
                                    "Someone has applied for: " + opportunity.title(),
                                    Notification.PRIORITY_NORMAL,
                                    Notification.STATUS_UNREAD,
                                    LocalDateTime.now(),
                                    Option.none(),
                                    Option.none(),
                                    Option.of("/applications/" + application.applicationId()),
                                    Option.of("VolunteerApplication"),
                                    Option.of(application.applicationId())
                                );
                                
                                notificationDAO.create(notification);
                            });
                        });
                    });
                });
            });
        });
    }
    
    // Request/Response records
    
    public record OpportunitySearchCriteria(
        String category,
        List<String> skills,
        Integer maxHoursPerWeek,
        String city,
        String province,
        Integer volunteerAge,
        String sortBy
    ) {}
    
    public record ApplicationDetails(
        String motivation,
        String experience,
        String availability
    ) {}
    
    public record VolunteerStatistics(
        int totalApplications,
        int acceptedApplications,
        int pendingApplications,
        int totalHours,
        int uniqueCategories,
        List<String> categories
    ) {}
}