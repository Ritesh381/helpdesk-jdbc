package com.helpdesk.service;

import com.helpdesk.dao.TicketDAO;
import com.helpdesk.dao.CustomerDAO;
import com.helpdesk.dao.AgentDAO;
import com.helpdesk.model.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * Service layer for Help Desk Platform business logic
 */
public class HelpDeskService {
    
    private TicketDAO ticketDAO;
    private CustomerDAO customerDAO;
    private AgentDAO agentDAO;
    
    public HelpDeskService() {
        this.ticketDAO = new TicketDAO();
        this.customerDAO = new CustomerDAO();
        this.agentDAO = new AgentDAO();
    }
    
    /**
     * Create a new ticket with initial message
     */
    public int createTicketWithMessage(int customerId, int categoryId, int priorityId, String initialMessage) throws SQLException {
        // Create ticket with status 1 (Open)
        Ticket ticket = new Ticket(customerId, 1, categoryId, priorityId);
        int ticketId = ticketDAO.createTicket(ticket);
        
        // Add initial message from customer
        TicketConversation message = new TicketConversation(ticketId, initialMessage, false, customerId);
        ticketDAO.addMessage(message);
        
        return ticketId;
    }
    
    /**
     * Assign ticket to best available agent
     */
    public boolean assignTicketToAgent(int ticketId, int agentId) throws SQLException {
        // Assign ticket
        boolean assigned = ticketDAO.assignTicket(ticketId, agentId);
        
        if (assigned) {
            // Update agent metrics
            agentDAO.updateAgentMetricsOnAssignment(agentId);
        }
        
        return assigned;
    }
    
    /**
     * Resolve ticket and update metrics
     */
    public boolean resolveTicket(int ticketId, String resolutionMessage, int agentId) throws SQLException {
        // Get ticket details for handle time calculation
        Ticket ticket = ticketDAO.getTicketById(ticketId);
        if (ticket == null || ticket.getAssignedAt() == null) {
            return false;
        }
        
        // Add resolution message
        TicketConversation message = new TicketConversation(ticketId, resolutionMessage, true, agentId);
        ticketDAO.addMessage(message);
        
        // Update ticket status to resolved (status 3)
        boolean resolved = ticketDAO.updateTicketStatus(ticketId, 3);
        
        if (resolved) {
            // Calculate handle time in minutes
            long handleTime = (System.currentTimeMillis() - ticket.getAssignedAt().getTime()) / (1000 * 60);
            
            // Update agent metrics
            agentDAO.updateAgentMetricsOnResolution(agentId, handleTime);
        }
        
        return resolved;
    }
    
    /**
     * Close ticket (customer or agent can close)
     */
    public boolean closeTicket(int ticketId) throws SQLException {
        return ticketDAO.updateTicketStatus(ticketId, 4); // Status 4 = Closed
    }
    
    /**
     * Add message to existing ticket conversation
     */
    public boolean addMessageToTicket(int ticketId, String messageText, boolean isFromAgent, int senderId) throws SQLException {
        TicketConversation message = new TicketConversation(ticketId, messageText, isFromAgent, senderId);
        return ticketDAO.addMessage(message);
    }
    
    /**
     * Register new customer
     */
    public int registerCustomer(String name, String email) throws SQLException {
        // Check if email already exists
        if (customerDAO.emailExists(email)) {
            throw new SQLException("Customer with this email already exists");
        }
        
        Customer customer = new Customer(name, email);
        return customerDAO.createCustomer(customer);
    }
    
    /**
     * Register new agent
     */
    public int registerAgent(String name, String email, Timestamp availableFrom, Timestamp availableTo) throws SQLException {
        Agent agent = new Agent(name, email, availableFrom, availableTo);
        return agentDAO.createAgent(agent);
    }
    
    /**
     * Add skill to agent
     */
    public boolean addSkillToAgent(int agentId, int categoryId) throws SQLException {
        return agentDAO.addAgentSkill(agentId, categoryId);
    }
    
    /**
     * Get full ticket details with conversation
     */
    public TicketDetails getFullTicketDetails(int ticketId) throws SQLException {
        Ticket ticket = ticketDAO.getTicketById(ticketId);
        if (ticket == null) {
            return null;
        }
        
        List<TicketConversation> conversations = ticketDAO.getConversationHistory(ticketId);
        
        return new TicketDetails(ticket, conversations);
    }
    
    /**
     * Get agents with highest resolution rate
     */
    public List<Object[]> getTopPerformingAgents() throws SQLException {
        return agentDAO.getAgentsWithHighestResolutionRate();
    }
    
    /**
     * Get average resolution time per category
     */
    public List<Object[]> getCategoryPerformanceReport() throws SQLException {
        return ticketDAO.getAverageResolutionTimePerCategory();
    }
    
    /**
     * Generate monthly ticket volume report
     */
    public List<Object[]> getMonthlyVolumeReport(int year, int month) throws SQLException {
        return ticketDAO.getMonthlyTicketVolumeReport(year, month);
    }
    
    /**
     * Get conversation history for a ticket
     */
    public List<TicketConversation> getTicketConversation(int ticketId) throws SQLException {
        return ticketDAO.getConversationHistory(ticketId);
    }
    
    /**
     * Get all customers
     */
    public List<Customer> getAllCustomers() throws SQLException {
        return customerDAO.getAllCustomers();
    }
    
    /**
     * Get all agents
     */
    public List<Agent> getAllAgents() throws SQLException {
        return agentDAO.getAllAgents();
    }
    
    /**
     * Get customer by ID
     */
    public Customer getCustomerById(int customerId) throws SQLException {
        return customerDAO.getCustomerById(customerId);
    }
    
    /**
     * Get agent by ID
     */
    public Agent getAgentById(int agentId) throws SQLException {
        return agentDAO.getAgentById(agentId);
    }
    
    /**
     * Inner class to hold ticket with conversation details
     */
    public static class TicketDetails {
        private Ticket ticket;
        private List<TicketConversation> conversations;
        
        public TicketDetails(Ticket ticket, List<TicketConversation> conversations) {
            this.ticket = ticket;
            this.conversations = conversations;
        }
        
        public Ticket getTicket() {
            return ticket;
        }
        
        public List<TicketConversation> getConversations() {
            return conversations;
        }
    }
}