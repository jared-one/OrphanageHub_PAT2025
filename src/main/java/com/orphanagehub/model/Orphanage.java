package com.orphanagehub.model;

import io.vavr.control.Option;

/**
 * Represents an Orphanage, as per Phase 2 UML.
 * Immutable with getters/setters via with-methods (FP style).
 */
public record Orphanage(String orphanageId, String name, String address, String contactPerson,
                        Option<String> contactEmail, Option<String> contactPhone, String verificationStatus) {

    public static final String ORPHANAGE_TYPE = "Standard"; // UML constant

    /**
     * Gets formatted details.
     * @return Details string.
     */
    public String getDetails() {
        return name + " at " + address + ", Contact: " + contactPerson;
    }

    // Immutable setter (returns new instance)
    public Orphanage withName(String newName) {
        return new Orphanage(orphanageId, newName, address, contactPerson, contactEmail, contactPhone, verificationStatus);
    }

    public Orphanage withAddress(String newAddress) {
        return new Orphanage(orphanageId, name, newAddress, contactPerson, contactEmail, contactPhone, verificationStatus);
    }

    public Orphanage withContactPerson(String newContact) {
        return new Orphanage(orphanageId, name, address, newContact, contactEmail, contactPhone, verificationStatus);
    }

    public Orphanage withVerificationStatus(String newStatus) {
        return new Orphanage(orphanageId, name, address, contactPerson, contactEmail, contactPhone, newStatus);
    }

    public Orphanage withOrphanageId(String newId) {
        return new Orphanage(newId, name, address, contactPerson, contactEmail, contactPhone, verificationStatus);
    }
}
