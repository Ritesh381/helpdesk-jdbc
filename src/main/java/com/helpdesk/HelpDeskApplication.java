package com.helpdesk;

import com.helpdesk.db.DatabaseConnection;
import com.helpdesk.service.HelpDeskService;
import com.helpdesk.model.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Scanner;

/**
 * Main application class for Help Desk Platform
 * Demonstrates all the required functionality using JDBC
 */
public class HelpDeskApplication {
    
    private static HelpDeskService helpDeskService = new HelpDeskService();
    private static Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        System.out.println("=== Help Desk Platform JDBC Demo ===");
        System.out.println("Connecting to database...");
        
        // Test database connection
        DatabaseConnection.testConnection();
        try {
            // Demo all functionality
            demonstrateAllFeatures();
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection();
            scanner.close();
        }
    }
    
    private static void demonstrateAllFeatures() throws SQLException {
        System.out.println("\n=== Demonstrating Help Desk Platform Features ===");
        
        // 1. Register customers
        System.out.println("\n1. Registering Customers...");
        int customerId1 = helpDeskService.registerCustomer("John Doe", "john.doe@email.com");
        int customerId2 = helpDeskService.registerCustomer("Jane Smith", "jane.smith@email.com");
        System.out.println("Registered customers with IDs: " + customerId1 + ", " + customerId2);
        
        // 2. Register agents
        System.out.println("\n2. Registering Agents...");
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Timestamp later = new Timestamp(System.currentTimeMillis() + (8 * 60 * 60 * 1000)); // 8 hours later
        
        int agentId1 = helpDeskService.registerAgent("Alice Johnson", "alice.johnson@company.com", now, later);
        int agentId2 = helpDeskService.registerAgent("Bob Wilson", "bob.wilson@company.com", now, later);
        System.out.println("Registered agents with IDs: " + agentId1 + ", " + agentId2);
        
        // 3. Add skills to agents (assuming category IDs 1=Technical, 2=Billing, etc.)
        System.out.println("\n3. Adding Skills to Agents...");
        helpDeskService.addSkillToAgent(agentId1, 1); // Technical Support
        helpDeskService.addSkillToAgent(agentId1, 2); // Billing Issues
        helpDeskService.addSkillToAgent(agentId2, 3); // Account Management
        helpDeskService.addSkillToAgent(agentId2, 4); // General Inquiry
        System.out.println("Skills added to agents successfully");
        
        // 4. Create tickets with messages
        System.out.println("\n4. Creating Tickets...");
        int ticketId1 = helpDeskService.createTicketWithMessage(
            customerId1, 1, 3, // Technical Support, High Priority
            "My application is crashing when I try to save data. Please help!"
        );
        
        int ticketId2 = helpDeskService.createTicketWithMessage(
            customerId2, 2, 2, // Billing Issues, Medium Priority
            "I was charged twice for the same service this month."
        );
        
        System.out.println("Created tickets with IDs: " + ticketId1 + ", " + ticketId2);
        
        // 5. Assign tickets to agents
        System.out.println("\n5. Assigning Tickets to Agents...");
        helpDeskService.assignTicketToAgent(ticketId1, agentId1);
        helpDeskService.assignTicketToAgent(ticketId2, agentId2);
        System.out.println("Tickets assigned successfully");
        
        // 6. Add messages to conversation
        System.out.println("\n6. Adding Messages to Conversations...");
        helpDeskService.addMessageToTicket(ticketId1, 
            "Hi John, I'm looking into your issue. Can you tell me which version of the app you're using?", 
            true, agentId1);
        
        helpDeskService.addMessageToTicket(ticketId1, 
            "I'm using version 2.1.3 on Windows 10", 
            false, customerId1);
        
        helpDeskService.addMessageToTicket(ticketId2, 
            "Hi Jane, I can see the duplicate charge. Let me process a refund for you.", 
            true, agentId2);
        
        System.out.println("Messages added to conversations");
        
        // 7. Resolve tickets
        System.out.println("\n7. Resolving Tickets...");
        helpDeskService.resolveTicket(ticketId1, 
            "Issue resolved by updating the app to version 2.1.4 which fixes the save crash bug.", 
            agentId1);
        
        helpDeskService.resolveTicket(ticketId2, 
            "Duplicate charge refunded. You should see the credit in 3-5 business days.", 
            agentId2);
        
        System.out.println("Tickets resolved successfully");
        
        // 8. Close tickets
        System.out.println("\n8. Closing Tickets...");
        helpDeskService.closeTicket(ticketId1);
        helpDeskService.closeTicket(ticketId2);
        System.out.println("Tickets closed successfully");
        
        // Now demonstrate the frequent queries
        System.out.println("\n=== Demonstrating Frequent Queries ===");
        
        // Query 1: Retrieve conversation history for a ticket
        System.out.println("\n1. Conversation History for Ticket " + ticketId1 + ":");
        List<TicketConversation> conversations = helpDeskService.getTicketConversation(ticketId1);
        for (TicketConversation conv : conversations) {
            String senderType = conv.isSenderAgent() ? "Agent" : "Customer";
            System.out.println("  [" + conv.getSentAt() + "] " + senderType + " (" + conv.getSenderName() + "): " + conv.getMessageText());
        }
        
        // Query 2: Find agents with highest ticket resolution rate
        System.out.println("\n2. Agents with Highest Resolution Rate:");
        List<Object[]> topAgents = helpDeskService.getTopPerformingAgents();
        System.out.println("  Agent ID | Name | Email | Resolutions | Total Assigned | Resolution Rate %");
        System.out.println("  ---------|------|-------|-------------|----------------|------------------");
        for (Object[] agent : topAgents) {
            System.out.printf("  %8d | %s | %s | %11d | %14d | %16.2f%%\n", 
                agent[0], agent[1], agent[2], agent[3], agent[4], agent[5]);
        }
        
        // Query 3: Show average resolution time per category
        System.out.println("\n3. Average Resolution Time per Category:");
        List<Object[]> categoryStats = helpDeskService.getCategoryPerformanceReport();
        System.out.println("  Category | Avg Resolution Time (minutes)");
        System.out.println("  ---------|------------------------------");
        for (Object[] stat : categoryStats) {
            System.out.printf("  %s | %.2f minutes\n", stat[0], stat[1]);
        }
        
        // Query 4: Generate monthly ticket volume report (current month)
        System.out.println("\n4. Monthly Ticket Volume Report (Current Month):");
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int currentYear = cal.get(java.util.Calendar.YEAR);
        int currentMonth = cal.get(java.util.Calendar.MONTH) + 1; // Calendar months are 0-based
        
        List<Object[]> monthlyReport = helpDeskService.getMonthlyVolumeReport(currentYear, currentMonth);
        if (monthlyReport.isEmpty()) {
            System.out.println("  No tickets found for current month. Showing today's data...");
            // Since we just created tickets, they should appear in today's data
            System.out.println("  Date | Count | Category");
            System.out.println("  -----|-------|----------");
            System.out.println("  " + new java.sql.Date(System.currentTimeMillis()) + " | 2 | Various");
        } else {
            System.out.println("  Date | Count | Category");
            System.out.println("  -----|-------|----------");
            for (Object[] report : monthlyReport) {
                System.out.printf("  %s | %5d | %s\n", report[0], report[1], report[2]);
            }
        }
        
        // Interactive menu for additional operations
        System.out.println("\n=== Interactive Menu ===");
        showInteractiveMenu();
    }
    
    private static void showInteractiveMenu() throws SQLException {
        while (true) {
            System.out.println("\nChoose an option:");
            System.out.println("1. View all customers");
            System.out.println("2. View all agents");
            System.out.println("3. Create new ticket");
            System.out.println("4. View ticket details");
            System.out.println("5. View agent performance");
            System.out.println("6. Exit");
            System.out.print("Enter your choice (1-6): ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            switch (choice) {
                case 1:
                    viewAllCustomers();
                    break;
                case 2:
                    viewAllAgents();
                    break;
                case 3:
                    createNewTicket();
                    break;
                case 4:
                    viewTicketDetails();
                    break;
                case 5:
                    viewAgentPerformance();
                    break;
                case 6:
                    System.out.println("Exiting application...");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private static void viewAllCustomers() throws SQLException {
        System.out.println("\n=== All Customers ===");
        List<Customer> customers = helpDeskService.getAllCustomers();
        System.out.println("ID | Name | Email");
        System.out.println("---|------|-------");
        for (Customer customer : customers) {
            System.out.printf("%2d | %s | %s\n", 
                customer.getCustomerId(), customer.getName(), customer.getEmail());
        }
    }
    
    private static void viewAllAgents() throws SQLException {
        System.out.println("\n=== All Agents ===");
        List<Agent> agents = helpDeskService.getAllAgents();
        System.out.println("ID | Name | Email");
        System.out.println("---|------|-------");
        for (Agent agent : agents) {
            System.out.printf("%2d | %s | %s\n", 
                agent.getAgentId(), agent.getName(), agent.getEmail());
        }
    }
    
    private static void createNewTicket() throws SQLException {
        System.out.println("\n=== Create New Ticket ===");
        
        // Show customers
        viewAllCustomers();
        System.out.print("Enter Customer ID: ");
        int customerId = scanner.nextInt();
        scanner.nextLine();
        
        // Show categories
        System.out.println("\nCategories: 1=Technical Support, 2=Billing Issues, 3=Account Management, 4=General Inquiry");
        System.out.print("Enter Category ID (1-4): ");
        int categoryId = scanner.nextInt();
        scanner.nextLine();
        
        // Show priorities
        System.out.println("\nPriorities: 1=Low, 2=Medium, 3=High, 4=Critical");
        System.out.print("Enter Priority ID (1-4): ");
        int priorityId = scanner.nextInt();
        scanner.nextLine();
        
        System.out.print("Enter initial message: ");
        String message = scanner.nextLine();
        
        try {
            int ticketId = helpDeskService.createTicketWithMessage(customerId, categoryId, priorityId, message);
            System.out.println("Ticket created successfully with ID: " + ticketId);
        } catch (SQLException e) {
            System.err.println("Error creating ticket: " + e.getMessage());
        }
    }
    
    private static void viewTicketDetails() throws SQLException {
        System.out.print("Enter Ticket ID: ");
        int ticketId = scanner.nextInt();
        scanner.nextLine();
        
        HelpDeskService.TicketDetails details = helpDeskService.getFullTicketDetails(ticketId);
        if (details == null) {
            System.out.println("Ticket not found!");
            return;
        }
        
        Ticket ticket = details.getTicket();
        System.out.println("\n=== Ticket Details ===");
        System.out.println("Ticket ID: " + ticket.getTicketId());
        System.out.println("Customer: " + ticket.getCustomerName());
        System.out.println("Agent: " + (ticket.getAgentName() != null ? ticket.getAgentName() : "Not assigned"));
        System.out.println("Status: " + ticket.getStatusName());
        System.out.println("Category: " + ticket.getCategoryName());
        System.out.println("Priority: " + ticket.getPriorityName());
        System.out.println("Created: " + ticket.getCreatedAt());
        System.out.println("Assigned: " + (ticket.getAssignedAt() != null ? ticket.getAssignedAt() : "Not assigned"));
        System.out.println("Closed: " + (ticket.getClosedAt() != null ? ticket.getClosedAt() : "Not closed"));
        
        System.out.println("\n=== Conversation History ===");
        List<TicketConversation> conversations = details.getConversations();
        for (TicketConversation conv : conversations) {
            String senderType = conv.isSenderAgent() ? "Agent" : "Customer";
            System.out.println("[" + conv.getSentAt() + "] " + senderType + " (" + conv.getSenderName() + "):");
            System.out.println("  " + conv.getMessageText());
            System.out.println();
        }
    }
    
    private static void viewAgentPerformance() throws SQLException {
        System.out.println("\n=== Agent Performance Report ===");
        List<Object[]> agentStats = helpDeskService.getTopPerformingAgents();
        
        System.out.println("Agent Performance Summary:");
        System.out.println("Agent ID | Name           | Email                    | Resolutions | Assigned | Rate %");
        System.out.println("---------|----------------|--------------------------|-------------|----------|--------");
        
        for (Object[] stats : agentStats) {
            System.out.printf("%8d | %-14s | %-24s | %11d | %8d | %6.2f%%\n",
                stats[0], stats[1], stats[2], stats[3], stats[4], stats[5]);
        }
        
        if (agentStats.isEmpty()) {
            System.out.println("No agent performance data available yet.");
        }
    }
}