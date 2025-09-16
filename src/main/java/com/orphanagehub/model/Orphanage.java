package com.orphanagehub.model;

import io.vavr.control.Option;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Immutable Orphanage model representing the TblOrphanages table.
 * Includes all fields from the expanded database schema.
 */
public record Orphanage(
    Integer orphanageId,
    Option<String> orphanageName,
    Option<String> registrationNumber,
    Option<String> taxNumber,
    String address,
    String city,
    String province,
    Option<String> postalCode,
    String contactPerson,
    String contactEmail,
    String contactPhone,
    Option<String> alternatePhone,
    Option<String> website,
    Option<String> description,
    Option<String> mission,
    Option<String> vision,
    Option<LocalDate> establishedDate,
    Option<Integer> capacity,
    Option<Integer> currentOccupancy,
    Option<Integer> ageGroupMin,
    Option<Integer> ageGroupMax,
    boolean acceptsDonations,
    boolean acceptsVolunteers,
    Option<String> bankName,
    Option<String> bankAccountNumber,
    Option<String> bankBranchCode,
    LocalDateTime dateRegistered,
    String verificationStatus,
    Option<LocalDateTime> verificationDate,
    Option<Integer> verifiedBy,
    Option<String> verificationNotes,
    Integer userId,
    String status,
    Option<String> logo,
    Option<String> coverImage,
    Option<Double> latitude,
    Option<Double> longitude,
    Option<LocalDateTime> modifiedDate,
    Option<Integer> modifiedBy
) {
    
    public static final String DEFAULT_STATUS = "Active";
    public static final String DEFAULT_VERIFICATION_STATUS = "Pending";
    
    /**
     * Creates an Orphanage with minimal required fields.
     */
    public static Orphanage createBasic(String name, String address, String city,
                                       String province, String contactPerson,
                                       String contactEmail, String contactPhone,
                                       Integer userId) {
        return new Orphanage(
            null, Option.of(name), Option.none(), Option.none(), address, city, province,
            Option.none(), contactPerson, contactEmail, contactPhone, Option.none(),
            Option.none(), Option.none(), Option.none(), Option.none(), Option.none(),
            Option.none(), Option.none(), Option.none(), Option.none(), true, true,
            Option.none(), Option.none(), Option.none(), LocalDateTime.now(),
            DEFAULT_VERIFICATION_STATUS, Option.none(), Option.none(), Option.none(),
            userId, DEFAULT_STATUS, Option.none(), Option.none(), Option.none(),
            Option.none(), Option.none(), Option.none()
        );
    }
    
    /**
     * Creates an Orphanage with extended fields.
     */
    public static Orphanage createExtended(
            String name, String registrationNumber, String address, String city,
            String province, String postalCode, String contactPerson,
            String contactEmail, String contactPhone, String description,
            Integer capacity, Integer userId) {
        return new Orphanage(
            null, Option.of(name), Option.of(registrationNumber), Option.none(), 
            address, city, province, Option.of(postalCode), contactPerson, 
            contactEmail, contactPhone, Option.none(), Option.none(), 
            Option.of(description), Option.none(), Option.none(), Option.none(),
            Option.of(capacity), Option.none(), Option.none(), Option.none(), 
            true, true, Option.none(), Option.none(), Option.none(), 
            LocalDateTime.now(), DEFAULT_VERIFICATION_STATUS, Option.none(), 
            Option.none(), Option.none(), userId, DEFAULT_STATUS, Option.none(), 
            Option.none(), Option.none(), Option.none(), Option.none(), Option.none()
        );
    }
    
    /**
     * Gets formatted orphanage details.
     */
    public String getDetails() {
        return String.format("%s at %s, %s, %s - Contact: %s (%s)",
            orphanageName.getOrElse("Unknown"), address, city, province, contactPerson, contactEmail);
    }
    
    /**
     * Gets full address as a single string.
     */
    public String getFullAddress() {
        StringBuilder addr = new StringBuilder(address);
        addr.append(", ").append(city);
        addr.append(", ").append(province);
        postalCode.forEach(pc -> addr.append(", ").append(pc));
        return addr.toString();
    }
    
    // ============================================
    // GUI COMPATIBILITY METHODS - FIXED
    // ============================================
    
    /**
     * Name accessor for GUI compatibility - returns String directly
     */
    public String name() {
        return orphanageName.getOrElse("Unknown");
    }
    
    /**
     * Orphanage name accessor - returns Option<String>
     */
    public Option<String> orphanageNameOption() {
        return orphanageName;
    }
    
    /**
     * Get orphanage name as String (for GUI compatibility)
     */
    public String getOrphanageName() {
        return orphanageName.getOrElse("Unknown");
    }
    
    /**
     * Email accessor for GUI compatibility
     */
    public String email() {
        return contactEmail;
    }
    
    /**
     * Phone number accessor for GUI compatibility
     */
    public String phoneNumber() {
        return contactPhone;
    }
    
    /**
     * Registration number accessor (returns String directly)
     */
    public String registrationNumberStr() {
        return registrationNumber.getOrElse("");
    }
    
    /**
     * Tax number accessor (returns String directly)
     */
    public String taxNumberStr() {
        return taxNumber.getOrElse("");
    }
    
    /**
     * Postal code accessor (returns String directly)
     */
    public String postalCodeStr() {
        return postalCode.getOrElse("");
    }
    
    /**
     * Alternate phone accessor (returns String directly)
     */
    public String alternatePhoneStr() {
        return alternatePhone.getOrElse("");
    }
    
    /**
     * Website accessor (returns String directly)
     */
    public String websiteStr() {
        return website.getOrElse("");
    }
    
    /**
     * Description accessor (returns String directly)
     */
    public String descriptionStr() {
        return description.getOrElse("");
    }
    
    /**
     * Mission accessor (returns String directly)
     */
    public String missionStr() {
        return mission.getOrElse("");
    }
    
    /**
     * Vision accessor (returns String directly)
     */
    public String visionStr() {
        return vision.getOrElse("");
    }
    
    /**
     * Bank name accessor (returns String directly)
     */
    public String bankNameStr() {
        return bankName.getOrElse("");
    }
    
    /**
     * Bank account number accessor (returns String directly)
     */
    public String bankAccountNumberStr() {
        return bankAccountNumber.getOrElse("");
    }
    
    /**
     * Bank branch code accessor (returns String directly)
     */
    public String bankBranchCodeStr() {
        return bankBranchCode.getOrElse("");
    }
    
    // ============================================
    // STATUS CHECKING METHODS
    // ============================================
    
    /**
     * Checks if orphanage is verified.
     */
    public boolean isVerified() {
        return "Verified".equalsIgnoreCase(verificationStatus);
    }
    
    /**
     * Checks if orphanage is pending verification.
     */
    public boolean isPendingVerification() {
        return "Pending".equalsIgnoreCase(verificationStatus);
    }
    
    /**
     * Checks if orphanage is under review.
     */
    public boolean isUnderReview() {
        return "Under Review".equalsIgnoreCase(verificationStatus);
    }
    
    /**
     * Checks if orphanage is active.
     */
    public boolean isActive() {
        return "Active".equalsIgnoreCase(status);
    }
    
    /**
     * Checks if orphanage is suspended.
     */
    public boolean isSuspended() {
        return "Suspended".equalsIgnoreCase(status);
    }
    
    /**
     * Checks if orphanage has capacity for more children.
     */
    public boolean hasCapacity() {
        if (capacity.isEmpty() || currentOccupancy.isEmpty()) {
            return true; // Unknown capacity, assume available
        }
        return currentOccupancy.get() < capacity.get();
    }
    
    /**
     * Gets remaining capacity.
     */
    public Option<Integer> getRemainingCapacity() {
        if (capacity.isEmpty() || currentOccupancy.isEmpty()) {
            return Option.none();
        }
        return Option.of(capacity.get() - currentOccupancy.get());
    }
    
    /**
     * Gets occupancy percentage.
     */
    public Option<Double> getOccupancyPercentage() {
        if (capacity.isEmpty() || currentOccupancy.isEmpty() || capacity.get() == 0) {
            return Option.none();
        }
        return Option.of((currentOccupancy.get() * 100.0) / capacity.get());
    }
    
    /**
     * Checks if age is within accepted range.
     */
    public boolean acceptsAge(int age) {
        int minAge = ageGroupMin.getOrElse(0);
        int maxAge = ageGroupMax.getOrElse(18);
        return age >= minAge && age <= maxAge;
    }
    
    /**
     * Gets age range as formatted string.
     */
    public String getAgeRangeStr() {
        int minAge = ageGroupMin.getOrElse(0);
        int maxAge = ageGroupMax.getOrElse(18);
        return minAge + " - " + maxAge + " years";
    }
    
    /**
     * Gets capacity as string.
     */
    public String getCapacityStr() {
        return capacity.map(String::valueOf).getOrElse("Not specified");
    }
    
    /**
     * Gets current occupancy as string.
     */
    public String getCurrentOccupancyStr() {
        return currentOccupancy.map(String::valueOf).getOrElse("Not specified");
    }
    
    /**
     * Gets established date as string.
     */
    public String getEstablishedDateStr() {
        return establishedDate.map(LocalDate::toString).getOrElse("Not specified");
    }
    
    /**
     * Gets years in operation.
     */
    public Option<Integer> getYearsInOperation() {
        return establishedDate.map(date -> 
            java.time.Period.between(date, LocalDate.now()).getYears());
    }
    
    /**
     * Checks if orphanage has banking details.
     */
    public boolean hasBankingDetails() {
        return bankName.isDefined() && 
               bankAccountNumber.isDefined() && 
               bankBranchCode.isDefined();
    }
    
    /**
     * Gets location coordinates.
     */
    public Option<String> getCoordinates() {
        if (latitude.isDefined() && longitude.isDefined()) {
            return Option.of(String.format("%.6f, %.6f", latitude.get(), longitude.get()));
        }
        return Option.none();
    }
    
    // ============================================
    // IMMUTABLE UPDATE METHODS
    // ============================================
    
    /**
     * Updates verification status.
     */
    public Orphanage withVerificationStatus(String newStatus, Integer verifiedById) {
        return new Orphanage(
            orphanageId, orphanageName, registrationNumber, taxNumber, address, city, province,
            postalCode, contactPerson, contactEmail, contactPhone, alternatePhone, website,
            description, mission, vision, establishedDate, capacity, currentOccupancy,
            ageGroupMin, ageGroupMax, acceptsDonations, acceptsVolunteers, bankName,
            bankAccountNumber, bankBranchCode, dateRegistered, newStatus,
            Option.of(LocalDateTime.now()), Option.of(verifiedById), verificationNotes,
            userId, status, logo, coverImage, latitude, longitude,
            Option.of(LocalDateTime.now()), Option.of(verifiedById)
        );
    }
    
    /**
     * Updates status.
     */
    public Orphanage withStatus(String newStatus) {
        return new Orphanage(
            orphanageId, orphanageName, registrationNumber, taxNumber, address, city, province,
            postalCode, contactPerson, contactEmail, contactPhone, alternatePhone, website,
            description, mission, vision, establishedDate, capacity, currentOccupancy,
            ageGroupMin, ageGroupMax, acceptsDonations, acceptsVolunteers, bankName,
            bankAccountNumber, bankBranchCode, dateRegistered, verificationStatus,
            verificationDate, verifiedBy, verificationNotes, userId, newStatus, logo,
            coverImage, latitude, longitude, Option.of(LocalDateTime.now()), modifiedBy
        );
    }
    
    /**
     * Updates capacity and occupancy.
     */
    public Orphanage withCapacityAndOccupancy(Integer newCapacity, Integer newOccupancy) {
        return new Orphanage(
            orphanageId, orphanageName, registrationNumber, taxNumber, address, city, province,
            postalCode, contactPerson, contactEmail, contactPhone, alternatePhone, website,
            description, mission, vision, establishedDate, Option.of(newCapacity),
            Option.of(newOccupancy), ageGroupMin, ageGroupMax, acceptsDonations,
            acceptsVolunteers, bankName, bankAccountNumber, bankBranchCode, dateRegistered,
            verificationStatus, verificationDate, verifiedBy, verificationNotes, userId,
            status, logo, coverImage, latitude, longitude, Option.of(LocalDateTime.now()),
            modifiedBy
        );
    }
    
    /**
     * Updates contact information.
     */
    public Orphanage withContactInfo(String newContactPerson, String newContactEmail, 
                                    String newContactPhone) {
        return new Orphanage(
            orphanageId, orphanageName, registrationNumber, taxNumber, address, city, province,
            postalCode, newContactPerson, newContactEmail, newContactPhone, alternatePhone,
            website, description, mission, vision, establishedDate, capacity, currentOccupancy,
            ageGroupMin, ageGroupMax, acceptsDonations, acceptsVolunteers, bankName,
            bankAccountNumber, bankBranchCode, dateRegistered, verificationStatus,
            verificationDate, verifiedBy, verificationNotes, userId, status, logo, coverImage,
            latitude, longitude, Option.of(LocalDateTime.now()), modifiedBy
        );
    }
    
    /**
     * Updates banking details.
     */
    public Orphanage withBankingDetails(String newBankName, String newAccountNumber, 
                                       String newBranchCode) {
        return new Orphanage(
            orphanageId, orphanageName, registrationNumber, taxNumber, address, city, province,
            postalCode, contactPerson, contactEmail, contactPhone, alternatePhone, website,
            description, mission, vision, establishedDate, capacity, currentOccupancy,
            ageGroupMin, ageGroupMax, acceptsDonations, acceptsVolunteers,
            Option.of(newBankName), Option.of(newAccountNumber), Option.of(newBranchCode),
            dateRegistered, verificationStatus, verificationDate, verifiedBy, verificationNotes,
            userId, status, logo, coverImage, latitude, longitude,
            Option.of(LocalDateTime.now()), modifiedBy
        );
    }
}