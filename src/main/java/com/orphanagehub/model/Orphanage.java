package com.orphanagehub.model;

public class Orphanage() {
  private String orphanageID,
      registeredByUserID,
      name,
      address,
      contactPerson,
      contactEmail,
      contactPhone,
      description,
      verificationStatus;

  public String getOrphanageID() {
    return orphanageID;
  }

  public void setOrphanageID(String orphanageID) {
    this.orphanageID = orphanageID;
  }

  public String getRegisteredByUserID() {
    return registeredByUserID;
  }

  public void setRegisteredByUserID(String registeredByUserID) {
    this.registeredByUserID = registeredByUserID;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getContactPerson() {
    return contactPerson;
  }

  public void setContactPerson(String contactPerson) {
    this.contactPerson = contactPerson;
  }

  public String getContactEmail() {
    return contactEmail;
  }

  public void setContactEmail(String contactEmail) {
    this.contactEmail = contactEmail;
  }

  public String getContactPhone() {
    return contactPhone;
  }

  public void setContactPhone(String contactPhone) {
    this.contactPhone = contactPhone;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getVerificationStatus() {
    return verificationStatus;
  }

  public void setVerificationStatus(String verificationStatus) {
    this.verificationStatus = verificationStatus;
  }

  @Override
  public String toString() {
    return this.name;
  }
}