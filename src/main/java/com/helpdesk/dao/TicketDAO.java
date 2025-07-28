package com.helpdesk.dao;

import com.helpdesk.db.DatabaseConnection;
import com.helpdesk.model.Ticket;
import com.helpdesk.model.TicketConversation;
import com.helpdesk.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Ticket operations
 */
public class TicketDAO {
    
    /**
     * Create a new ticket
     */
    public int createTicket(Ticket ticket) throws SQLException {
        String sql = "INSERT INTO Tickets (customer_id, status_id, category_id, priority_id, created_at) " +
                    "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, ticket.getCustomerId());
            pstmt.setInt(2, ticket.getStatusId());
            pstmt.setInt(3, ticket.getCategoryId());
            pstmt.setInt(4, ticket.getPriorityId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating ticket failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating ticket failed, no ID obtained.");
                }
            }
        }
    }
    
    /**
     * Assign ticket to an agent
     */
    public boolean assignTicket(int ticketId, int agentId) throws SQLException {
        String sql = "UPDATE Tickets SET agent_id = ?, assigned_at = CURRENT_TIMESTAMP, status_id = 2 " +
                    "WHERE ticket_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, agentId);
            pstmt.setInt(2, ticketId);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Update ticket status
     */
    public boolean updateTicketStatus(int ticketId, int statusId) throws SQLException {
        String sql = "UPDATE Tickets SET status_id = ?" +
                    (statusId == 4 ? ", closed_at = CURRENT_TIMESTAMP" : "") + // Status 4 = Closed
                    " WHERE ticket_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, statusId);
            pstmt.setInt(2, ticketId);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Get ticket by ID with detailed information
     */
    public Ticket getTicketById(int ticketId) throws SQLException {
        String sql = "SELECT t.*, c.name as customer_name, a.name as agent_name, " +
                    "ts.status_name, tc.category_name, tp.priority_name " +
                    "FROM Tickets t " +
                    "JOIN Customers c ON t.customer_id = c.customer_id " +
                    "LEFT JOIN Agents a ON t.agent_id = a.agent_id " +
                    "JOIN TicketStatuses ts ON t.status_id = ts.status_id " +
                    "JOIN TicketCategories tc ON t.category_id = tc.category_id " +
                    "JOIN TicketPriorities tp ON t.priority_id = tp.priority_id " +
                    "WHERE t.ticket_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, ticketId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTicket(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Get conversation history for a ticket
     */
    public List<TicketConversation> getConversationHistory(int ticketId) throws SQLException {
        String sql = "SELECT tc.*, " +
                    "CASE WHEN tc.is_sender_agent = true THEN a.name ELSE c.name END as sender_name " +
                    "FROM TicketConversations tc " +
                    "LEFT JOIN Agents a ON tc.is_sender_agent = true AND tc.sender_id = a.agent_id " +
                    "LEFT JOIN Customers c ON tc.is_sender_agent = false AND tc.sender_id = c.customer_id " +
                    "WHERE tc.ticket_id = ? " +
                    "ORDER BY tc.sent_at ASC";
        
        List<TicketConversation> conversations = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, ticketId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TicketConversation conversation = new TicketConversation();
                    conversation.setMessageId(rs.getInt("message_id"));
                    conversation.setTicketId(rs.getInt("ticket_id"));
                    conversation.setMessageText(rs.getString("message_text"));
                    conversation.setSentAt(rs.getTimestamp("sent_at"));
                    conversation.setSenderAgent(rs.getBoolean("is_sender_agent"));
                    conversation.setSenderId(rs.getInt("sender_id"));
                    conversation.setSenderName(rs.getString("sender_name"));
                    
                    conversations.add(conversation);
                }
            }
        }
        
        return conversations;
    }
    
    /**
     * Add message to ticket conversation
     */
    public boolean addMessage(TicketConversation message) throws SQLException {
        String sql = "INSERT INTO TicketConversations (ticket_id, message_text, is_sender_agent, sender_id, sent_at) " +
                    "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, message.getTicketId());
            pstmt.setString(2, message.getMessageText());
            pstmt.setBoolean(3, message.isSenderAgent());
            pstmt.setInt(4, message.getSenderId());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Get monthly ticket volume report
     */
    public List<Object[]> getMonthlyTicketVolumeReport(int year, int month) throws SQLException {
        String sql = "SELECT DATE(created_at) as ticket_date, COUNT(*) as ticket_count, " +
                    "tc.category_name " +
                    "FROM Tickets t " +
                    "JOIN TicketCategories tc ON t.category_id = tc.category_id " +
                    "WHERE YEAR(created_at) = ? AND MONTH(created_at) = ? " +
                    "GROUP BY DATE(created_at), tc.category_name " +
                    "ORDER BY ticket_date";
        
        List<Object[]> report = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, year);
            pstmt.setInt(2, month);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = {
                        rs.getDate("ticket_date"),
                        rs.getInt("ticket_count"),
                        rs.getString("category_name")
                    };
                    report.add(row);
                }
            }
        }
        
        return report;
    }
    
    /**
     * Get average resolution time per category
     */
    public List<Object[]> getAverageResolutionTimePerCategory() throws SQLException {
        String sql = "SELECT tc.category_name, " +
                    "AVG(TIMESTAMPDIFF(MINUTE, t.created_at, t.closed_at)) as avg_resolution_time " +
                    "FROM Tickets t " +
                    "JOIN TicketCategories tc ON t.category_id = tc.category_id " +
                    "WHERE t.closed_at IS NOT NULL " +
                    "GROUP BY tc.category_id, tc.category_name " +
                    "ORDER BY avg_resolution_time";
        
        List<Object[]> report = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = {
                        rs.getString("category_name"),
                        rs.getDouble("avg_resolution_time")
                    };
                    report.add(row);
                }
            }
        }
        
        return report;
    }
    
    /**
     * Helper method to map ResultSet to Ticket object
     */
    private Ticket mapResultSetToTicket(ResultSet rs) throws SQLException {
        Ticket ticket = new Ticket();
        ticket.setTicketId(rs.getInt("ticket_id"));
        ticket.setCustomerId(rs.getInt("customer_id"));
        
        // Handle nullable agent_id
        int agentId = rs.getInt("agent_id");
        if (!rs.wasNull()) {
            ticket.setAgentId(agentId);
        }
        
        ticket.setStatusId(rs.getInt("status_id"));
        ticket.setCategoryId(rs.getInt("category_id"));
        ticket.setPriorityId(rs.getInt("priority_id"));
        ticket.setCreatedAt(rs.getTimestamp("created_at"));
        ticket.setAssignedAt(rs.getTimestamp("assigned_at"));
        ticket.setEscalated(rs.getBoolean("is_escalated"));
        ticket.setClosedAt(rs.getTimestamp("closed_at"));
        
        // Set display fields
        ticket.setCustomerName(rs.getString("customer_name"));
        ticket.setAgentName(rs.getString("agent_name"));
        ticket.setStatusName(rs.getString("status_name"));
        ticket.setCategoryName(rs.getString("category_name"));
        ticket.setPriorityName(rs.getString("priority_name"));
        
        return ticket;
    }
}