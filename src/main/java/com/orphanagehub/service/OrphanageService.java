package com.orphanagehub.service;

import com.orphanagehub.dao.OrphanageDAO;
import com.orphanagehub.dao.ResourceRequestDAO;
import com.orphanagehub.dao.VolunteerOpportunityDAO;
import com.orphanagehub.model.Orphanage;
import com.orphanagehub.model.ResourceRequest;
import com.orphanagehub.model.VolunteerOpportunity;
import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;

/**
 * Service for orphanage-related operations.
 * Manages profiles, requests, and opportunities.
 */
public class OrphanageService {

    private final OrphanageDAO orphanageDAO = new OrphanageDAO();
    private final ResourceRequestDAO requestDAO = new ResourceRequestDAO();
    private final VolunteerOpportunityDAO opportunityDAO = new VolunteerOpportunityDAO();

    /**
     * Creates or updates an orphanage profile.
     * @param orphanage The Orphanage to save.
     * @return Try<Orphanage> - saved orphanage on success, failure on error.
     */
   public Try<Orphanage> saveProfile(Orphanage orphanage) {
    if (orphanage.orphanageId() != null) {
        return orphanageDAO.update(orphanage)
            .map(v -> orphanage);  // Return the orphanage, not void
    } else {
        return orphanageDAO.create(orphanage);
    }
}

    /**
     * Adds or edits a resource request.
     * @param request The ResourceRequest to manage.
     * @return Try<ResourceRequest> - saved request on success, failure on error.
     */
    public Try<ResourceRequest> manageResourceRequest(ResourceRequest request) {
        return Try.of(() -> {
            if (request.quantity() <= 0) {
                throw new IllegalArgumentException("Quantity needed must be positive");
            }
            
            // Validate required fields
            if (request.resourceType() == null || request.resourceType().isEmpty()) {
                throw new IllegalArgumentException("Resource type is required");
            }
            
            if (request.resourceDescription() == null || request.resourceDescription().isEmpty()) {
                throw new IllegalArgumentException("Resource description is required");
            }
            
            return true;
        })
        .flatMap(valid -> {
            if (request.requestId() != null) {
                return requestDAO.update(request);
            } else {
                return requestDAO.create(request);
            }
        });
    }

    /**
     * Gets all requests for an orphanage.
     * @param orphanageId The ID to filter.
     * @return Try<List<ResourceRequest>> - immutable list of requests.
     */
    public Try<List<ResourceRequest>> getRequests(Integer orphanageId) {
        return requestDAO.findByOrphanageId(orphanageId);
    }

    /**
     * Posts or manages a volunteer opportunity.
     * @param opportunity The VolunteerOpportunity to post.
     * @return Try<VolunteerOpportunity> - saved opportunity on success, failure on error.
     */
  public Try<VolunteerOpportunity> manageVolunteerOpportunity(VolunteerOpportunity opportunity) {
    if (opportunity.opportunityId() != null) {
        return opportunityDAO.update(opportunity)
            .map(v -> opportunity);  // Return the opportunity, not void
    } else {
        return opportunityDAO.create(opportunity)
            .map(v -> opportunity);
    }
}
// Add method to get requests by orphanage ID (convert String to Integer)
public Try<List<ResourceRequest>> getResourceRequests(String orphanageId) {
    return requestDAO.findByOrphanageId(Integer.valueOf(orphanageId));
}


    /**
     * Acknowledges a donation by updating the request fulfillment.
     * @param requestId The request ID to update
     * @param fulfilledAmount The amount fulfilled
     * @return Try<ResourceRequest> - updated request on success, failure on error
     */
    public Try<ResourceRequest> acknowledgeDonation(Integer requestId, Double fulfilledAmount) {
        return requestDAO.findById(requestId)
            .flatMap(optReq -> optReq.toTry(() -> 
                new IllegalArgumentException("Request not found")))
            .flatMap(req -> {
                // Calculate new fulfilled amount
                Double currentFulfilled = req.quantityFulfilled();
                Double newFulfilled = currentFulfilled + fulfilledAmount;
                
                // Update request with new fulfillment status
                ResourceRequest updated = req.withQuantityFulfilled(newFulfilled);
                return requestDAO.update(updated);
            });
    }
    
    /**
     * Gets orphanage by ID
     * @param orphanageId The orphanage ID
     * @return Try<Option<Orphanage>> - orphanage if found
     */
    public Try<Option<Orphanage>> getOrphanageById(Integer orphanageId) {
        return orphanageDAO.findById(orphanageId);
    }
    
    /**
     * Gets orphanage by user ID
     * @param userId The user ID
     * @return Try<Option<Orphanage>> - orphanage if found
     */
    public Try<Option<Orphanage>> getOrphanageByUserId(Integer userId) {
        return orphanageDAO.findByUserId(userId);
    }

    public Try<Option<Orphanage>> getOrphanageByOpportunityId(Integer opportunityId) {
        return new com.orphanagehub.dao.VolunteerOpportunityDAO().findById(opportunityId)
            .flatMap(optOpp -> optOpp.toTry(() -> new RuntimeException("Opportunity not found")))
            .flatMap(opp -> orphanageDAO.findById(opp.orphanageId()));
    }
    
    /**
     * Gets all verified orphanages
     * @return Try<List<Orphanage>> - list of verified orphanages
     */
    public Try<List<Orphanage>> getVerifiedOrphanages() {
        return orphanageDAO.findAllVerified();
    }

    public Try<List<String>> getAllProvinces() {
        return orphanageDAO.getAllProvinces();
    }
    
    /**
     * Search orphanages by criteria
     * @param city Optional city filter
     * @param province Optional province filter
     * @return Try<List<Orphanage>> - filtered list of orphanages
     */
    public Try<List<Orphanage>> searchOrphanages(Option<String> city, Option<String> province) {
        return orphanageDAO.findAll()
            .map(orphanages -> {
                List<Orphanage> filtered = orphanages;
                
                // Filter by city if provided
                if (city.isDefined()) {
                    filtered = filtered.filter(o -> 
                        o.city().equalsIgnoreCase(city.get()));
                }
                
                // Filter by province if provided
                if (province.isDefined()) {
                    filtered = filtered.filter(o -> 
                        o.province().equalsIgnoreCase(province.get()));
                }
                
                // Only return verified and active orphanages
                filtered = filtered.filter(o -> 
                    o.isVerified() && o.isActive());
                
                return filtered;
            });
    }
}