package com.helpdesk.dao;

import com.helpdesk.db.DatabaseConnection;
import com.helpdesk.model.Agent;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Agent operations
 */
public class AgentDAO {
    // Creating a new agent
    public int createAgent(Agent agent) throws SQLException {
        String sql = "INSERT INTO Agents (name, email, available_from, available_to) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, agent.getName());
            pstmt.setString(2, agent.getEmail());
            pstmt.setTimestamp(3, agent.getAvailableFrom());
            pstmt.setTimestamp(4, agent.getAvailableTo());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating agent failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int agentId = generatedKeys.getInt(1);
                    // Initialize agent metrics
                    initializeAgentMetrics(agentId);
                    return agentId;
                } else {
                    throw new SQLException("Creating agent failed, no ID obtained.");
                }
            }
        }
    }
    
    /**
     * Initialize agent metrics for a new agent
     */
    private void initializeAgentMetrics(int agentId) throws SQLException {
        String sql = "INSERT INTO AgentMetrics (agent_id, total_resolutions, total_tickets_assigned, total_handle_time, is_assigned) " +
                    "VALUES (?, 0, 0, 0, true)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, agentId);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Get agent by ID
     */
    public Agent getAgentById(int agentId) throws SQLException {
        String sql = "SELECT * FROM Agents WHERE agent_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, agentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAgent(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Get agent by email
     */
    public Agent getAgentByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM Agents WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAgent(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Get all agents
     */
    public List<Agent> getAllAgents() throws SQLException {
        String sql = "SELECT * FROM Agents ORDER BY name";
        
        List<Agent> agents = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                agents.add(mapResultSetToAgent(rs));
            }
        }
        
        return agents;
    }
    
    /**
     * Update agent information
     */
    public boolean updateAgent(Agent agent) throws SQLException {
        String sql = "UPDATE Agents SET name = ?, email = ?, available_from = ?, available_to = ? WHERE agent_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, agent.getName());
            pstmt.setString(2, agent.getEmail());
            pstmt.setTimestamp(3, agent.getAvailableFrom());
            pstmt.setTimestamp(4, agent.getAvailableTo());
            pstmt.setInt(5, agent.getAgentId());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Find agents with highest ticket resolution rate
     */
    public List<Object[]> getAgentsWithHighestResolutionRate() throws SQLException {
        String sql = "SELECT a.agent_id, a.name, a.email, " +
                    "am.total_resolutions, am.total_tickets_assigned, " +
                    "CASE WHEN am.total_tickets_assigned > 0 THEN " +
                    "ROUND((am.total_resolutions * 100.0 / am.total_tickets_assigned), 2) " +
                    "ELSE 0 END as resolution_rate " +
                    "FROM Agents a " +
                    "JOIN AgentMetrics am ON a.agent_id = am.agent_id " +
                    "WHERE am.total_tickets_assigned > 0 " +
                    "ORDER BY resolution_rate DESC, am.total_resolutions DESC";
        
        List<Object[]> result = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("agent_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getInt("total_resolutions"),
                    rs.getInt("total_tickets_assigned"),
                    rs.getDouble("resolution_rate")
                };
                result.add(row);
            }
        }
        
        return result;
    }
    
    /**
     * Update agent metrics when a ticket is assigned
     */
    public boolean updateAgentMetricsOnAssignment(int agentId) throws SQLException {
        String sql = "UPDATE AgentMetrics SET total_tickets_assigned = total_tickets_assigned + 1 " +
                    "WHERE agent_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, agentId);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Update agent metrics when a ticket is resolved
     */
    public boolean updateAgentMetricsOnResolution(int agentId, long handleTimeMinutes) throws SQLException {
        String sql = "UPDATE AgentMetrics SET " +
                    "total_resolutions = total_resolutions + 1, " +
                    "total_handle_time = total_handle_time + ? " +
                    "WHERE agent_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, handleTimeMinutes);
            pstmt.setInt(2, agentId);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Add skill to agent
     */
    public boolean addAgentSkill(int agentId, int categoryId) throws SQLException {
        String sql = "INSERT INTO AgentSkills (agent_id, skill_category_id) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE agent_id = agent_id"; // Ignore if already exists
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, agentId);
            pstmt.setInt(2, categoryId);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Remove skill from agent
     */
    public boolean removeAgentSkill(int agentId, int categoryId) throws SQLException {
        String sql = "DELETE FROM AgentSkills WHERE agent_id = ? AND skill_category_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, agentId);
            pstmt.setInt(2, categoryId);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Get agents with specific skill (category)
     */
    public List<Agent> getAgentsBySkill(int categoryId) throws SQLException {
        String sql = "SELECT a.* FROM Agents a " +
                    "JOIN AgentSkills as_skill ON a.agent_id = as_skill.agent_id " +
                    "WHERE as_skill.skill_category_id = ? " +
                    "ORDER BY a.name";
        
        List<Agent> agents = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, categoryId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    agents.add(mapResultSetToAgent(rs));
                }
            }
        }
        
        return agents;
    }
    
    /**
     * Get available agents (those currently within their availability window)
     */
    public List<Agent> getAvailableAgents() throws SQLException {
        String sql = "SELECT a.* FROM Agents a " +
                    "JOIN AgentMetrics am ON a.agent_id = am.agent_id " +
                    "WHERE am.is_assigned = true " +
                    "AND (a.available_from IS NULL OR a.available_from <= CURRENT_TIMESTAMP) " +
                    "AND (a.available_to IS NULL OR a.available_to >= CURRENT_TIMESTAMP) " +
                    "ORDER BY a.name";
        
        List<Agent> agents = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                agents.add(mapResultSetToAgent(rs));
            }
        }
        
        return agents;
    }
    
    /**
     * Get agent skills (categories they can handle)
     */
    public List<Integer> getAgentSkills(int agentId) throws SQLException {
        String sql = "SELECT skill_category_id FROM AgentSkills WHERE agent_id = ?";
        
        List<Integer> skills = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, agentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    skills.add(rs.getInt("skill_category_id"));
                }
            }
        }
        
        return skills;
    }
    
    /**
     * Check if email already exists
     */
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Agents WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    /**
     * Get agent performance metrics
     */
    public Object[] getAgentMetrics(int agentId) throws SQLException {
        String sql = "SELECT a.name, am.total_resolutions, am.total_tickets_assigned, " +
                    "am.total_handle_time, am.is_assigned, " +
                    "CASE WHEN am.total_tickets_assigned > 0 THEN " +
                    "ROUND((am.total_resolutions * 100.0 / am.total_tickets_assigned), 2) " +
                    "ELSE 0 END as resolution_rate, " +
                    "CASE WHEN am.total_resolutions > 0 THEN " +
                    "ROUND((am.total_handle_time * 1.0 / am.total_resolutions), 2) " +
                    "ELSE 0 END as avg_handle_time " +
                    "FROM Agents a " +
                    "JOIN AgentMetrics am ON a.agent_id = am.agent_id " +
                    "WHERE a.agent_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, agentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Object[] {
                        rs.getString("name"),
                        rs.getInt("total_resolutions"),
                        rs.getInt("total_tickets_assigned"),
                        rs.getLong("total_handle_time"),
                        rs.getBoolean("is_assigned"),
                        rs.getDouble("resolution_rate"),
                        rs.getDouble("avg_handle_time")
                    };
                }
            }
        }
        
        return null;
    }
    
    /**
     * Set agent availability status
     */
    public boolean setAgentAvailability(int agentId, boolean isAvailable) throws SQLException {
        String sql = "UPDATE AgentMetrics SET is_assigned = ? WHERE agent_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setBoolean(1, isAvailable);
            pstmt.setInt(2, agentId);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Delete agent (only if no tickets assigned)
     */
    public boolean deleteAgent(int agentId) throws SQLException {
        // First check if agent has any assigned tickets
        String checkSql = "SELECT COUNT(*) FROM Tickets WHERE agent_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
            
            checkPstmt.setInt(1, agentId);
            
            try (ResultSet rs = checkPstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Cannot delete agent with assigned tickets");
                }
            }
            
            // Delete agent metrics first (foreign key constraint)
            String deleteMetricsSql = "DELETE FROM AgentMetrics WHERE agent_id = ?";
            try (PreparedStatement deleteMetricsPstmt = conn.prepareStatement(deleteMetricsSql)) {
                deleteMetricsPstmt.setInt(1, agentId);
                deleteMetricsPstmt.executeUpdate();
            }
            
            // Delete agent skills
            String deleteSkillsSql = "DELETE FROM AgentSkills WHERE agent_id = ?";
            try (PreparedStatement deleteSkillsPstmt = conn.prepareStatement(deleteSkillsSql)) {
                deleteSkillsPstmt.setInt(1, agentId);
                deleteSkillsPstmt.executeUpdate();
            }
            
            // Finally delete the agent
            String deleteAgentSql = "DELETE FROM Agents WHERE agent_id = ?";
            try (PreparedStatement deleteAgentPstmt = conn.prepareStatement(deleteAgentSql)) {
                deleteAgentPstmt.setInt(1, agentId);
                return deleteAgentPstmt.executeUpdate() > 0;
            }
        }
    }
    
    /**
     * Helper method to map ResultSet to Agent object
     */
    private Agent mapResultSetToAgent(ResultSet rs) throws SQLException {
        Agent agent = new Agent();
        agent.setAgentId(rs.getInt("agent_id"));
        agent.setName(rs.getString("name"));
        agent.setEmail(rs.getString("email"));
        agent.setAvailableFrom(rs.getTimestamp("available_from"));
        agent.setAvailableTo(rs.getTimestamp("available_to"));
        return agent;
    }
}