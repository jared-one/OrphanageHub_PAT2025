package com.orphanagehub.model;

import java.sql.Timestamp;

public class ResourceRequest() {
  public static final String DEFAULTSTATUS = "Open";
  private String requestID,
      orphanageID,
      postedByUserID,
      itemCategory,
      itemDescription,
      urgency,
      status;
  private int quantityNeeded, quantityFulfilled;
  private Timestamp datePosted;

  public String getRequestID() {
    return requestID;
  }

  public void setRequestID(String requestID) {
    this.requestID = requestID;
  }

  public String getOrphanageID() {
    return orphanageID;
  }

  public void setOrphanageID(String orphanageID) {
    this.orphanageID = orphanageID;
  }

  public String getPostedByUserID() {
    return postedByUserID;
  }

  public void setPostedByUserID(String postedByUserID) {
    this.postedByUserID = postedByUserID;
  }

  public String getItemCategory() {
    return itemCategory;
  }

  public void setItemCategory(String itemCategory) {
    this.itemCategory = itemCategory;
  }

  public String getItemDescription() {
    return itemDescription;
  }

  public void setItemDescription(String itemDescription) {
    this.itemDescription = itemDescription;
  }

  public int getQuantityNeeded() {
    return quantityNeeded;
  }

  public void setQuantityNeeded(int quantityNeeded) {
    this.quantityNeeded = quantityNeeded;
  }

  public int getQuantityFulfilled() {
    return quantityFulfilled;
  }

  public void setQuantityFulfilled(int quantityFulfilled) {
    this.quantityFulfilled = quantityFulfilled;
  }

  public String getUrgency() {
    return urgency;
  }

  public void setUrgency(String urgency) {
    this.urgency = urgency;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Timestamp getDatePosted() {
    return datePosted;
  }

  public void setDatePosted(Timestamp datePosted) {
    this.datePosted = datePosted;
  }
}