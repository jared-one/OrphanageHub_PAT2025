package com.orphanagehub.service;

import com.orphanagehub.dao.*;
import com.orphanagehub.model.*;
import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * Enhanced donor service with complete donation management.
 * 
 * @author OrphanageHub Team
 * @version 2.0
 * @since 2025-09-06
 */
public class DonorService {
    private static final Logger logger = LoggerFactory.getLogger(DonorService.class);
    
    private final DonationDAO donationDAO = new DonationDAO();
    private final DonationItemDAO itemDAO = new DonationItemDAO();
    private final OrphanageDAO orphanageDAO = new OrphanageDAO();
    private final ResourceRequestDAO requestDAO = new ResourceRequestDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();
    private final UserDAO userDAO = new UserDAO();
    
    private final GeometryFactory geometryFactory = new GeometryFactory();
    
    /**
     * Makes a monetary donation
     */
    public Try<Donation> makeMonetaryDonation(MonetaryDonationRequest request, Integer donorId) {
        // Validate amount
        if (request.amount() <= 0) {
            return Try.failure(new ServiceException("Donation amount must be positive"));
        }
        
        if (request.amount() > 1000000) {
            return Try.failure(new ServiceException("Donation amount exceeds maximum limit"));
        }
        
        // Create donation
        Donation donation = Donation.createMonetary(
            donorId,
            request.orphanageId(),
            request.amount(),
            request.paymentMethod()
        );
        
        // Add optional fields
        donation = new Donation(
            null,
            donation.donorId(),
            donation.orphanageId(),
            request.requestId().map(id -> id),
            donation.donationType(),
            donation.amount(),
            donation.currency(),
            Option.none(),
            Option.none(),
            Option.none(),
            donation.estimatedValue(),
            donation.donationDate(),
            request.scheduledDate().map(date -> date),
            donation.status(),
            donation.paymentMethod(),
            Option.none(), // Transaction ref set after payment
            request.taxDeductible(),
            request.anonymous(),
            request.recurring(),
            request.recurring() ? Option.of(request.recurrenceInterval()) : Option.none(),
            Option.none(), // Next recurrence calculated after first payment
            Option.of(request.donorMessage()),
            false,
            Option.none(),
            Option.none(),
            false,
            Option.of(request.notes()),
            LocalDateTime.now(),
            Option.none(),
            Option.none()
        );
        
        return donationDAO.create(donation)
            .flatMap(created -> {
                // Process payment
                return processPayment(created, request.paymentDetails())
                    .flatMap(transactionRef -> {
                        // Update donation with transaction reference
                        return donationDAO.markCompleted(created.donationId(), transactionRef)
                            .flatMap(v -> {
                                // Update resource request if applicable
                                if (created.requestId().isDefined()) {
                                    updateResourceRequest(created);
                                }
                                
                                // Notify orphanage
                                notifyOrphanageAboutDonation(created);
                                
                                // Create tax receipt if eligible
                                if (created.taxDeductible()) {
                                    generateTaxReceipt(created);
                                }
                                
                                // Schedule recurring if applicable
                                if (created.recurringDonation()) {
                                    scheduleRecurringDonation(created);
                                }
                                
                                // Log donation
                                auditLogDAO.logSuccess(donorId, null, AuditLog.ACTION_DONATE,
                                    "Donation", created.donationId().toString());
                                
                                logger.info("Monetary donation {} completed", created.donationId());
                                return donationDAO.findById(created.donationId())
                                    .flatMap(opt -> opt.toTry(() -> 
                                        new ServiceException("Donation not found after creation")));
                            });
                    });
            });
    }
    
    /**
     * Makes an item donation
     */
    public Try<Donation> makeItemDonation(ItemDonationRequest request, Integer donorId) {
        // Validate items
        if (request.items().isEmpty()) {
            return Try.failure(new ServiceException("At least one item is required"));
        }
        
        // Create donation
        Donation donation = new Donation(
            null,
            donorId,
            request.orphanageId(),
            request.requestId().map(id -> id),
            request.donationType(),
            Option.none(), // No monetary amount
            "ZAR",
            Option.of(request.description()),
            Option.of((double) request.items().size()),
            Option.of("items"),
            Option.of(request.estimatedValue()),
            LocalDateTime.now(),
            Option.of(request.scheduledPickupDate()),
            "Pending",
            Option.none(),
            Option.none(),
            true,
            request.anonymous(),
            false, // Items can't be recurring
            Option.none(),
            Option.none(),
            Option.of(request.donorMessage()),
            false,
            Option.none(),
            Option.none(),
            false,
            Option.of(request.notes()),
            LocalDateTime.now(),
            Option.none(),
            Option.none()
        );
        
        return donationDAO.create(donation)
            .flatMap(created -> {
                // Create donation items
                List<DonationItem> donationItems = request.items().map(item ->
                    DonationItem.create(
                        created.donationId(),
                        item.name(),
                        item.category(),
                        Double.valueOf(item.quantity()),
                        item.unit()
                    )
                );
                
                return itemDAO.createBatch(created.donationId(), donationItems)
                    .flatMap(items -> {
                        // Notify orphanage
                        notifyOrphanageAboutDonation(created);
                        
                        // Log donation
                        auditLogDAO.logSuccess(donorId, null, AuditLog.ACTION_DONATE,
                            "Donation", created.donationId().toString());
                        
                        logger.info("Item donation {} created with {} items", 
                            created.donationId(), items.size());
                        
                        return donationDAO.findById(created.donationId())
                            .flatMap(opt -> opt.toTry(() -> 
                                new ServiceException("Donation not found after creation")));
                    });
            });
    }
    
    /**
     * Searches orphanages with filters
     */
    public Try<List<Orphanage>> searchOrphanages(SearchCriteria criteria) {
        Try<List<Orphanage>> baseResults = orphanageDAO.findAllVerified();
        
        return baseResults.map(orphanages -> {
            List<Orphanage> filtered = orphanages;
            
            // Filter by province
            if (criteria.province() != null) {
                filtered = filtered.filter(o -> o.province().equals(criteria.province()));
            }
            
            // Filter by city
            if (criteria.city() != null) {
                filtered = filtered.filter(o -> o.city().equals(criteria.city()));
            }
            
            // Filter by location radius
            if (criteria.location() != null && criteria.radiusKm() != null) {
                filtered = filterByLocation(filtered, criteria.location(), criteria.radiusKm());
            }
            
            // Filter by accepts donations
            if (criteria.acceptsDonations() != null) {
                filtered = filtered.filter(o -> o.acceptsDonations() == criteria.acceptsDonations());
            }
            
            // Filter by capacity
            if (criteria.hasCapacity() != null && criteria.hasCapacity()) {
                filtered = filtered.filter(Orphanage::hasCapacity);
            }
            
            // Sort by criteria
            if (criteria.sortBy() != null) {
                filtered = sortOrphanages(filtered, criteria.sortBy());
            }
            
            return filtered;
        });
    }
    
    /**
     * Gets orphanage details with current needs
     */
    public Try<OrphanageDetailsWithNeeds> getOrphanageDetails(Integer orphanageId) {
        return orphanageDAO.findById(orphanageId)
            .flatMap(orphOpt -> orphOpt.toTry(() -> 
                new ServiceException("Orphanage not found")))
            .flatMap(orphanage -> {
                // Get current requests
                Try<List<ResourceRequest>> requests = requestDAO.findByOrphanageId(orphanageId)
                    .map(reqs -> reqs.filter(r -> r.isOpen()));
                
                // Get recent donations
                Try<List<Donation>> donations = donationDAO.findByOrphanage(orphanageId)
                    .map(dons -> dons.take(10)); // Last 10 donations
                
                // Get volunteer opportunities
                Try<List<VolunteerOpportunity>> opportunities = 
                    new VolunteerOpportunityDAO().findByOrphanageId(orphanageId)
                        .map(ops -> ops.filter(o -> o.isOpen()));
                
                // Get statistics
                Try<DonationDAO.DonationStatistics> stats = 
                    donationDAO.getStatistics(orphanageId);
                
                return requests.flatMap(reqs ->
                    donations.flatMap(dons ->
                        opportunities.flatMap(ops ->
                            stats.map(st -> 
                                new OrphanageDetailsWithNeeds(
                                    orphanage,
                                    reqs,
                                    dons,
                                    ops,
                                    st
                                )
                            )
                        )
                    )
                );
            });
    }
    
    /**
     * Gets donation history for donor
     */
    public Try<List<Donation>> getDonationHistory(Integer donorId) {
        return donationDAO.findByDonor(donorId);
    }
    
    /**
     * Gets donation statistics for donor
     */
    public Try<DonorStatistics> getDonorStatistics(Integer donorId) {
        return donationDAO.findByDonor(donorId)
            .map(donations -> {
                int totalDonations = donations.size();
                double totalAmount = donations
                    .filter(d -> d.donationType().equals(Donation.TYPE_MONEY))
                    .map(d -> d.amount().getOrElse(0.0))
                    .sum().doubleValue();
                
                int itemDonations = donations
                    .filter(d -> !d.donationType().equals(Donation.TYPE_MONEY))
                    .size();
                
                int recurringDonations = donations
                    .filter(Donation::recurringDonation)
                    .size();
                
                List<String> supportedOrphanages = donations
                    .map(Donation::orphanageId)
                    .distinct()
                    .map(Object::toString);
                
                return new DonorStatistics(
                    totalDonations,
                    totalAmount,
                    itemDonations,
                    recurringDonations,
                    supportedOrphanages.size(),
                    supportedOrphanages
                );
            });
    }

    public Try<List<Donation>> getDonationsForOrphanage(String orphanageId) {
        return Try.of(() -> Integer.valueOf(orphanageId))
            .flatMap(id -> donationDAO.findByOrphanage(id));
    }
    
    /**
     * Cancels a recurring donation
     */
    public Try<Void> cancelRecurringDonation(Integer donationId, Integer donorId) {
        return donationDAO.findById(donationId)
            .flatMap(donOpt -> donOpt.toTry(() -> 
                new ServiceException("Donation not found")))
            .flatMap(donation -> {
                // Verify ownership
                if (!donation.donorId().equals(donorId)) {
                    return Try.failure(new ServiceException("Unauthorized"));
                }
                
                if (!donation.recurringDonation()) {
                    return Try.failure(new ServiceException("Not a recurring donation"));
                }
                
                // Update donation to stop recurrence
                return donationDAO.cancelRecurring(donationId)
                    .flatMap(v -> {
                        // Notify orphanage
                        notifyOrphanageAboutCancellation(donation);
                        
                        // Log cancellation
                        auditLogDAO.logSuccess(donorId, null, AuditLog.ACTION_UPDATE,
                            "Donation", "Cancelled recurring: " + donationId);
                        
                        logger.info("Recurring donation {} cancelled", donationId);
                        return Try.success(null);
                    });
            });
    }
    
    // Helper methods
    
    private Try<String> processPayment(Donation donation, PaymentDetails details) {
        // In production, integrate with payment gateway
        // For now, simulate successful payment
        String transactionRef = "TXN" + System.currentTimeMillis();
        logger.info("Payment processed for donation {}: {}", donation.donationId(), transactionRef);
        return Try.success(transactionRef);
    }
    
    private void updateResourceRequest(Donation donation) {
        donation.requestId().forEach(requestId -> {
            requestDAO.findById(requestId).forEach(reqOpt -> {
                reqOpt.forEach(request -> {
                    // Update fulfilled amount
                    double fulfilledValue = request.actualValue().getOrElse(0.0) + 
                        donation.amount().getOrElse(0.0);
                    
                    ResourceRequest updated = request.withActualValue(fulfilledValue);
                    
                    // Check if fully fulfilled
                    if (fulfilledValue >= request.estimatedValue().getOrElse(Double.MAX_VALUE)) {
                        updated = updated.updateStatus("Fulfilled");
                    }
                    
                    requestDAO.update(updated);
                });
            });
        });
    }
    
    private void notifyOrphanageAboutDonation(Donation donation) {
        orphanageDAO.findById(donation.orphanageId()).forEach(orphOpt -> {
            orphOpt.forEach(orphanage -> {
                userDAO.findById(orphanage.userId()).forEach(userOpt -> {
                    userOpt.forEach(user -> {
                        String message = donation.anonymousDonation() 
                            ? "You received an anonymous donation"
                            : "You received a donation";
                        
                        if (donation.donationType().equals(Donation.TYPE_MONEY)) {
                            message += String.format(": R%.2f", donation.amount().getOrElse(0.0));
                        } else {
                            message += ": " + donation.itemDescription().getOrElse("Items");
                        }
                        
                        Notification notification = new Notification(
                            null,
                            user.userId(),
                            Notification.TYPE_DONATION,
                            "New Donation Received",
                            message,
                            Notification.PRIORITY_HIGH,
                            Notification.STATUS_UNREAD,
                            LocalDateTime.now(),
                            Option.none(),
                            Option.none(),
                            Option.of("/donations/" + donation.donationId()),
                            Option.of("Donation"),
                            Option.of(donation.donationId())
                        );
                        
                        notificationDAO.create(notification);
                    });
                });
            });
        });
    }
    
    private void notifyOrphanageAboutCancellation(Donation donation) {
        // Similar to above but for cancellation
        logger.debug("Notifying orphanage about donation cancellation");
    }
    
    private void generateTaxReceipt(Donation donation) {
        // Generate Section 18A tax receipt
        String receiptNumber = "18A-" + donation.donationId() + "-" + System.currentTimeMillis();
        logger.info("Tax receipt generated: {}", receiptNumber);
    }
    
    private void scheduleRecurringDonation(Donation donation) {
        // Schedule next recurring donation
        donation.recurrenceInterval().forEach(interval -> {
            LocalDateTime nextDate = calculateNextRecurrence(donation.donationDate(), interval);
            donationDAO.updateNextRecurrence(donation.donationId(), nextDate);
            logger.info("Scheduled next recurrence for donation {}: {}", 
                donation.donationId(), nextDate);
        });
    }
    
    private LocalDateTime calculateNextRecurrence(LocalDateTime lastDate, String interval) {
        return switch (interval.toLowerCase()) {
            case "weekly" -> lastDate.plusWeeks(1);
            case "monthly" -> lastDate.plusMonths(1);
            case "quarterly" -> lastDate.plusMonths(3);
            case "annually" -> lastDate.plusYears(1);
            default -> lastDate.plusMonths(1);
        };
    }
    
    private List<Orphanage> filterByLocation(List<Orphanage> orphanages, LocationPoint location, double radiusKm) {
        Point searchPoint = geometryFactory.createPoint(
            new Coordinate(location.longitude(), location.latitude())
        );
        
        return orphanages.filter(orphanage -> {
            if (orphanage.latitude().isEmpty() || orphanage.longitude().isEmpty()) {
                return true; // Include if no coordinates
            }
            
            Point orphPoint = geometryFactory.createPoint(
                new Coordinate(
                    orphanage.longitude().get(),
                    orphanage.latitude().get()
                )
            );
            
            double distance = calculateDistance(searchPoint, orphPoint);
            return distance <= radiusKm;
        });
    }
    
    private double calculateDistance(Point p1, Point p2) {
        // Haversine formula for distance in km
        double lat1 = p1.getY();
        double lon1 = p1.getX();
        double lat2 = p2.getY();
        double lon2 = p2.getX();
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return 6371 * c; // Earth radius in km
    }
    
  private List<Orphanage> sortOrphanages(List<Orphanage> orphanages, String sortBy) {
    return switch (sortBy.toLowerCase()) {
        case "name" -> orphanages.sortBy(o -> o.getOrphanageName());
        case "city" -> orphanages.sortBy(Orphanage::city);
        case "province" -> orphanages.sortBy(Orphanage::province);
        case "newest" -> orphanages.sortBy(Orphanage::dateRegistered).reverse();
        case "verified" -> orphanages.sortBy(o -> o.isVerified() ? 0 : 1);
        default -> orphanages;
    };
}
    // Request/Response records
    
    public record MonetaryDonationRequest(
        Integer orphanageId,
        Option<Integer> requestId,
        double amount,
        String paymentMethod,
        PaymentDetails paymentDetails,
        boolean taxDeductible,
        boolean anonymous,
        boolean recurring,
        String recurrenceInterval,
        Option<LocalDateTime> scheduledDate,
        String donorMessage,
        String notes
    ) {}
    
    public record ItemDonationRequest(
        Integer orphanageId,
        Option<Integer> requestId,
        String donationType,
        String description,
        List<ItemDetails> items,
        double estimatedValue,
        LocalDateTime scheduledPickupDate,
        boolean anonymous,
        String donorMessage,
        String notes
    ) {}
    
    public record ItemDetails(
        String name,
        String category,
        int quantity,
        String unit,
        String condition
    ) {}
    
    public record PaymentDetails(
        String cardNumber,
        String cardHolder,
        String expiryMonth,
        String expiryYear,
        String cvv
    ) {}
    
    public record SearchCriteria(
        String province,
        String city,
        LocationPoint location,
        Double radiusKm,
        Boolean acceptsDonations,
        Boolean hasCapacity,
        String sortBy
    ) {}
    
    public record LocationPoint(
        double latitude,
        double longitude
    ) {}
    
    public record OrphanageDetailsWithNeeds(
        Orphanage orphanage,
        List<ResourceRequest> currentNeeds,
        List<Donation> recentDonations,
        List<VolunteerOpportunity> volunteerOpportunities,
        DonationDAO.DonationStatistics statistics
    ) {}
    
    public record DonorStatistics(
        int totalDonations,
        double totalAmount,
        int itemDonations,
        int recurringDonations,
        int supportedOrphanages,
        List<String> orphanageIds
    ) {}

    public record DonationWithDonor(Donation donation, String donorName) {}

    public Try<List<DonationWithDonor>> getDonationsWithDonorForOrphanage(String orphanageId) {
        return Try.of(() -> Integer.valueOf(orphanageId))
            .flatMap(id -> donationDAO.findByOrphanage(id))
            .map(donations -> donations.map(donation -> {
                String donorName = userDAO.findById(donation.donorId())
                    .map(optUser -> optUser.map(User::username).getOrElse("Anonymous"))
                    .getOrElse("Anonymous");
                return new DonationWithDonor(donation, donorName);
            }));
    }
}