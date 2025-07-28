package com.helpdesk.service;

import com.helpdesk.model.Customer;
import com.helpdesk.model.Agent;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Basic unit tests for HelpDeskService
 * Note: These tests require a running MySQL database with the schema set up
 */
public class HelpDeskServiceTest {
    
    private HelpDeskService helpDeskService;
    
    @Before
    public void setUp() {
        helpDeskService = new HelpDeskService();
    }
    
    @Test
    public void testCustomerRegistration() {
        try {
            // Test customer registration
            int customerId = helpDeskService.registerCustomer("Test Customer", "test@example.com");
            assertTrue("Customer ID should be positive", customerId > 0);
            
            // Test retrieving the customer
            Customer customer = helpDeskService.getCustomerById(customerId);
            assertNotNull("Customer should not be null", customer);
            assertEquals("Customer name should match", "Test Customer", customer.getName());
            assertEquals("Customer email should match", "test@example.com", customer.getEmail());
            
        } catch (SQLException e) {
            fail("SQLException occurred: " + e.getMessage());
        }
    }
    
    @Test
    public void testAgentRegistration() {
        try {
            // Test agent registration
            Timestamp now = new Timestamp(System.currentTimeMillis());
            Timestamp later = new Timestamp(System.currentTimeMillis() + (8 * 60 * 60 * 1000));
            
            int agentId = helpDeskService.registerAgent("Test Agent", "agent@company.com", now, later);
            assertTrue("Agent ID should be positive", agentId > 0);
            
            // Test retrieving the agent
            Agent agent = helpDeskService.getAgentById(agentId);
            assertNotNull("Agent should not be null", agent);
            assertEquals("Agent name should match", "Test Agent", agent.getName());
            assertEquals("Agent email should match", "agent@company.com", agent.getEmail());
            
        } catch (SQLException e) {
            fail("SQLException occurred: " + e.getMessage());
        }
    }
    
    @Test
    public void testTicketCreation() {
        try {
            // First create a customer
            int customerId = helpDeskService.registerCustomer("Ticket Test Customer", "tickettest@example.com");
            
            // Create a ticket
            int ticketId = helpDeskService.createTicketWithMessage(
                customerId, 1, 2, "Test ticket message"
            );
            
            assertTrue("Ticket ID should be positive", ticketId > 0);
            
            // Verify conversation was created
            var conversations = helpDeskService.getTicketConversation(ticketId);
            assertFalse("Conversations should not be empty", conversations.isEmpty());
            assertEquals("Should have one message", 1, conversations.size());
            assertEquals("Message text should match", "Test ticket message", conversations.get(0).getMessageText());
            
        } catch (SQLException e) {
            fail("SQLException occurred: " + e.getMessage());
        }
    }
    
    @Test
    public void testTicketAssignment() {
        try {
            // Create customer and agent
            int customerId = helpDeskService.registerCustomer("Assignment Test Customer", "assigntest@example.com");
            
            Timestamp now = new Timestamp(System.currentTimeMillis());
            Timestamp later = new Timestamp(System.currentTimeMillis() + (8 * 60 * 60 * 1000));
            int agentId = helpDeskService.registerAgent("Assignment Test Agent", "assignagent@company.com", now, later);
            
            // Create ticket
            int ticketId = helpDeskService.createTicketWithMessage(
                customerId, 1, 2, "Assignment test message"
            );
            
            // Assign ticket
            boolean assigned = helpDeskService.assignTicketToAgent(ticketId, agentId);
            assertTrue("Ticket should be assigned successfully", assigned);
            
        } catch (SQLException e) {
            fail("SQLException occurred: " + e.getMessage());
        }
    }
    
    @Test(expected = SQLException.class)
    public void testDuplicateCustomerEmail() throws SQLException {
        // This should pass for the first customer
        helpDeskService.registerCustomer("Customer 1", "duplicate@example.com");
        
        // This should throw SQLException due to duplicate email
        helpDeskService.registerCustomer("Customer 2", "duplicate@example.com");
    }
    
    @Test
    public void testGetAllCustomers() {
        try {
            // Get initial count
            var initialCustomers = helpDeskService.getAllCustomers();
            int initialCount = initialCustomers.size();
            
            // Add a customer
            helpDeskService.registerCustomer("List Test Customer", "listtest@example.com");
            
            // Check count increased
            var afterCustomers = helpDeskService.getAllCustomers();
            assertEquals("Customer count should increase by 1", initialCount + 1, afterCustomers.size());
            
        } catch (SQLException e) {
            fail("SQLException occurred: " + e.getMessage());
        }
    }
    
    @Test
    public void testPerformanceReports() {
        try {
            // Test that reports can be generated without errors
            var topAgents = helpDeskService.getTopPerformingAgents();
            assertNotNull("Top agents report should not be null", topAgents);
            
            var categoryReport = helpDeskService.getCategoryPerformanceReport();
            assertNotNull("Category report should not be null", categoryReport);
            
            var monthlyReport = helpDeskService.getMonthlyVolumeReport(2024, 1);
            assertNotNull("Monthly report should not be null", monthlyReport);
            
        } catch (SQLException e) {
            fail("SQLException occurred: " + e.getMessage());
        }
    }
}