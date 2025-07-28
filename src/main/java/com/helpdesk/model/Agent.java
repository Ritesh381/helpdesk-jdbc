package com.helpdesk.model;

import java.sql.Timestamp;

/**
 * Agent model class representing the Agents table
 */
public class Agent {
    private int agentId;
    private String name;
    private String email;
    private Timestamp availableFrom;
    private Timestamp availableTo;
    
    // Default constructor
    public Agent() {}
    
    // Constructor with parameters
    public Agent(String name, String email, Timestamp availableFrom, Timestamp availableTo) {
        this.name = name;
        this.email = email;
        this.availableFrom = availableFrom;
        this.availableTo = availableTo;
    }
    
    public Agent(int agentId, String name, String email, Timestamp availableFrom, Timestamp availableTo) {
        this.agentId = agentId;
        this.name = name;
        this.email = email;
        this.availableFrom = availableFrom;
        this.availableTo = availableTo;
    }
    
    // Getters and Setters
    public int getAgentId() {
        return agentId;
    }
    
    public void setAgentId(int agentId) {
        this.agentId = agentId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Timestamp getAvailableFrom() {
        return availableFrom;
    }
    
    public void setAvailableFrom(Timestamp availableFrom) {
        this.availableFrom = availableFrom;
    }
    
    public Timestamp getAvailableTo() {
        return availableTo;
    }
    
    public void setAvailableTo(Timestamp availableTo) {
        this.availableTo = availableTo;
    }
    
    @Override
    public String toString() {
        return "Agent{" +
                "agentId=" + agentId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", availableFrom=" + availableFrom +
                ", availableTo=" + availableTo +
                '}';
    }
}