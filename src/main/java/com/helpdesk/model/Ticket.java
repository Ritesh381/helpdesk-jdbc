package com.helpdesk.model;

import java.sql.Timestamp;

/**
 * Ticket model class representing the Tickets table
 */
public class Ticket {
    private int ticketId;
    private int customerId;
    private Integer agentId; // Can be null
    private int statusId;
    private int categoryId;
    private int priorityId;
    private Timestamp createdAt;
    private Timestamp assignedAt;
    private boolean isEscalated;
    private Timestamp closedAt;
    
    // For display purposes - joined data
    private String customerName;
    private String agentName;
    private String statusName;
    private String categoryName;
    private String priorityName;
    
    // Default constructor
    public Ticket() {}
    
    // Constructor for creating new ticket
    public Ticket(int customerId, int statusId, int categoryId, int priorityId) {
        this.customerId = customerId;
        this.statusId = statusId;
        this.categoryId = categoryId;
        this.priorityId = priorityId;
        this.isEscalated = false;
    }
    
    // Getters and Setters
    public int getTicketId() {
        return ticketId;
    }
    
    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }
    
    public int getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }
    
    public Integer getAgentId() {
        return agentId;
    }
    
    public void setAgentId(Integer agentId) {
        this.agentId = agentId;
    }
    
    public int getStatusId() {
        return statusId;
    }
    
    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }
    
    public int getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
    
    public int getPriorityId() {
        return priorityId;
    }
    
    public void setPriorityId(int priorityId) {
        this.priorityId = priorityId;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public Timestamp getAssignedAt() {
        return assignedAt;
    }
    
    public void setAssignedAt(Timestamp assignedAt) {
        this.assignedAt = assignedAt;
    }
    
    public boolean isEscalated() {
        return isEscalated;
    }
    
    public void setEscalated(boolean escalated) {
        isEscalated = escalated;
    }
    
    public Timestamp getClosedAt() {
        return closedAt;
    }
    
    public void setClosedAt(Timestamp closedAt) {
        this.closedAt = closedAt;
    }
    
    // Display fields getters and setters
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public String getAgentName() {
        return agentName;
    }
    
    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }
    
    public String getStatusName() {
        return statusName;
    }
    
    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public String getPriorityName() {
        return priorityName;
    }
    
    public void setPriorityName(String priorityName) {
        this.priorityName = priorityName;
    }
    
    @Override
    public String toString() {
        return "Ticket{" +
                "ticketId=" + ticketId +
                ", customerId=" + customerId +
                ", agentId=" + agentId +
                ", statusId=" + statusId +
                ", categoryId=" + categoryId +
                ", priorityId=" + priorityId +
                ", createdAt=" + createdAt +
                ", assignedAt=" + assignedAt +
                ", isEscalated=" + isEscalated +
                ", closedAt=" + closedAt +
                ", customerName='" + customerName + '\'' +
                ", agentName='" + agentName + '\'' +
                ", statusName='" + statusName + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", priorityName='" + priorityName + '\'' +
                '}';
    }
}