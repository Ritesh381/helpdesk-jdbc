-- Help Desk Platform Database Schema
-- Switch from UUID to simple integer IDs for simplicity

CREATE DATABASE helpdesk_platform;
USE helpdesk_platform;

-- Customers table
CREATE TABLE Customers (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE
);

-- Ticket Statuses table
CREATE TABLE TicketStatuses (
    status_id INT PRIMARY KEY AUTO_INCREMENT,
    status_name VARCHAR(50) NOT NULL UNIQUE
);

-- Ticket Categories table
CREATE TABLE TicketCategories (
    category_id INT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(100) NOT NULL,
    max_response_time BIGINT NOT NULL, -- in minutes
    max_resolution_time BIGINT NOT NULL -- in minutes
);

-- Ticket Priorities table
CREATE TABLE TicketPriorities (
    priority_id INT PRIMARY KEY AUTO_INCREMENT,
    priority_name VARCHAR(50) NOT NULL UNIQUE
);

-- Agents table
CREATE TABLE Agents (
    agent_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    available_from DATETIME,
    available_to DATETIME
);

-- Agent Skills table (many-to-many relationship)
CREATE TABLE AgentSkills (
    agent_id INT,
    skill_category_id INT,
    PRIMARY KEY (agent_id, skill_category_id),
    FOREIGN KEY (agent_id) REFERENCES Agents(agent_id) ON DELETE CASCADE,
    FOREIGN KEY (skill_category_id) REFERENCES TicketCategories(category_id) ON DELETE CASCADE
);

-- Tickets table
CREATE TABLE Tickets (
    ticket_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    agent_id INT,
    status_id INT NOT NULL,
    category_id INT NOT NULL,
    priority_id INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    assigned_at DATETIME,
    is_escalated BOOLEAN DEFAULT FALSE,
    closed_at DATETIME,
    FOREIGN KEY (customer_id) REFERENCES Customers(customer_id),
    FOREIGN KEY (agent_id) REFERENCES Agents(agent_id),
    FOREIGN KEY (status_id) REFERENCES TicketStatuses(status_id),
    FOREIGN KEY (category_id) REFERENCES TicketCategories(category_id),
    FOREIGN KEY (priority_id) REFERENCES TicketPriorities(priority_id)
);

-- Ticket Conversations table
CREATE TABLE TicketConversations (
    message_id INT PRIMARY KEY AUTO_INCREMENT,
    ticket_id INT NOT NULL,
    message_text TEXT NOT NULL,
    sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_sender_agent BOOLEAN NOT FALSE,
    sender_id INT NOT NULL, -- can be customer_id or agent_id depending on is_sender_agent
    FOREIGN KEY (ticket_id) REFERENCES Tickets(ticket_id) ON DELETE CASCADE
);

-- Ticket History table (for audit trail)
CREATE TABLE TicketHistory (
    history_id INT PRIMARY KEY AUTO_INCREMENT,
    ticket_id INT NOT NULL,
    changed_by INT NOT NULL, -- agent_id who made the change
    changed_field_name VARCHAR(50) NOT NULL,
    old_value_id INT,
    new_value_id INT,
    changed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ticket_id) REFERENCES Tickets(ticket_id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES Agents(agent_id)
);

-- Ticket Feedback table
CREATE TABLE TicketFeedback (
    ticket_id INT PRIMARY KEY,
    ticket_rating INT CHECK (ticket_rating >= 1 AND ticket_rating <= 5),
    ticket_feedback TEXT,
    feedback_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ticket_id) REFERENCES Tickets(ticket_id) ON DELETE CASCADE
);

-- Agent Metrics table (for performance tracking)
CREATE TABLE AgentMetrics (
    agent_id INT PRIMARY KEY,
    total_resolutions INT DEFAULT 0,
    total_tickets_assigned INT DEFAULT 0,
    total_handle_time BIGINT DEFAULT 0, -- in minutes
    is_assigned BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (agent_id) REFERENCES Agents(agent_id) ON DELETE CASCADE
);

-- Category Metrics table (for category-wise analytics)
CREATE TABLE CategoryMetrics (
    category_id INT PRIMARY KEY,
    total_resolution_time BIGINT DEFAULT 0, -- in minutes
    total_resolutions INT DEFAULT 0,
    FOREIGN KEY (category_id) REFERENCES TicketCategories(category_id) ON DELETE CASCADE
);

-- Insert default data
INSERT INTO TicketStatuses (status_name) VALUES 
('Open'), ('In Progress'), ('Resolved'), ('Closed');

INSERT INTO TicketPriorities (priority_name) VALUES 
('Low'), ('Medium'), ('High'), ('Critical');

INSERT INTO TicketCategories (category_name, max_response_time, max_resolution_time) VALUES 
('Technical Support', 60, 480),    -- 1 hour response, 8 hours resolution
('Billing Issues', 30, 240),       -- 30 min response, 4 hours resolution
('Account Management', 120, 1440),  -- 2 hours response, 24 hours resolution
('General Inquiry', 240, 2880);    -- 4 hours response, 48 hours resolution