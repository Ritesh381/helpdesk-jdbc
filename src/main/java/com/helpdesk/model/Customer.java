package com.helpdesk.model;

/**
 * Customer model class representing the Customers table
 */
public class Customer {
    private int customerId;
    private String name;
    private String email;
    
    // Default constructor
    public Customer() {}
    
    // Constructor with parameters
    public Customer(String name, String email) {
        this.name = name;
        this.email = email;
    }
    
    public Customer(int customerId, String name, String email) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
    }
    
    // Getters and Setters
    public int getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
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
    
    @Override
    public String toString() {
        return "Customer{" +
                "customerId=" + customerId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}