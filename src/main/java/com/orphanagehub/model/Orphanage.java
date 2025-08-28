package com.orphanagehub.model;

public class Orphanage {
    private String orphanageId;
    private String userId;
    private String name;
    private String address;
    private String contactPerson;
    private String contactEmail;
    private String contactPhone;
    private String verificationStatus;

    public Orphanage(String orphanageId, String userId, String name, String address,
                     String contactPerson, String contactEmail, String contactPhone,
                     String verificationStatus) {
        this.orphanageId = orphanageId;
        this.userId = userId;
        this.name = name;
        this.address = address;
        this.contactPerson = contactPerson;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.verificationStatus = verificationStatus;
    }

    // Getters and Setters
    public String getOrphanageId() { return orphanageId; }
    public void setOrphanageId(String orphanageId) { this.orphanageId = orphanageId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }
}