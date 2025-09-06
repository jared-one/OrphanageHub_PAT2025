package com.orphanagehub.service;

import com.orphanagehub.dao.OrphanageDAO;
import com.orphanagehub.dao.ResourceRequestDAO;
import com.orphanagehub.dao.VolunteerOpportunityDAO;
import com.orphanagehub.model.Orphanage;
import com.orphanagehub.model.ResourceRequest;
import com.orphanagehub.model.VolunteerOpportunity;
import com.orphanagehub.util.ValidationUtil;
import io.vavr.collection.List;
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
     * @return Try<Void> - success on save, failure on error.
     */
    public Try<Void> saveProfile(Orphanage orphanage) {
        return orphanageDAO.findById(orphanage.orphanageId())
                .flatMap(opt -> opt.isDefined() ? orphanageDAO.update(orphanage).map(o -> (Void) null) : orphanageDAO.create(orphanage).map(o -> (Void) null));
    }

    /**
     * Adds or edits a resource request.
     * @param request The ResourceRequest to manage.
     * @return Try<Void> - success on operation, failure on error.
     */
    public Try<Void> manageResourceRequest(ResourceRequest request) {
        return Try.of(() -> {
                    if (request.quantityNeeded() <= 0) {
                        throw new IllegalArgumentException("Quantity needed must be positive");
                    }
                    return true;
                })
                .flatMap(valid -> (request.requestId() != null && !request.requestId().isEmpty()) ? requestDAO.update(request) : requestDAO.create(request));
    }

    /**
     * Gets all requests for an orphanage.
     * @param orphanageId The ID to filter.
     * @return Try<List<ResourceRequest>> - immutable list of requests.
     */
    public Try<List<ResourceRequest>> getRequests(String orphanageId) {
        return requestDAO.findByOrphanageId(orphanageId);
    }

    /**
     * Posts or manages a volunteer opportunity.
     * @param opportunity The VolunteerOpportunity to post.
     * @return Try<Void> - success on post, failure on error.
     */
    public Try<Void> manageVolunteerOpportunity(VolunteerOpportunity opportunity) {
        return (opportunity.opportunityId() != null && !opportunity.opportunityId().isEmpty()) ? 
            opportunityDAO.update(opportunity) : 
            opportunityDAO.create(opportunity);
    }

    // Additional: Acknowledge donation (Phase 1) - update request fulfilled
    public Try<Void> acknowledgeDonation(String requestId, int fulfilledAmount) {
        return requestDAO.findById(requestId)
                .flatMap(optReq -> optReq.toTry(() -> new IllegalArgumentException("Request not found")))
                .<ResourceRequest>map(req -> req.withQuantityFulfilled(req.quantityFulfilled() + fulfilledAmount))
                .flatMap(requestDAO::update);
    }
}