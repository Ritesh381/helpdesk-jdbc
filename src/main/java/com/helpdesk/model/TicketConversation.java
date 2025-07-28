package com.helpdesk.model;

import java.sql.Timestamp;

/**
 * TicketConversation model class representing the TicketConversations table
 */
public class TicketConversation {
    private int messageId;
    private int ticketId;
    private String messageText;
    private Timestamp sentAt;
    private boolean isSenderAgent;
    private int senderId;
    
    // For display purposes
    private String senderName;
    
    // Default constructor
    public TicketConversation() {}
    
    // Constructor for creating new message
    public TicketConversation(int ticketId, String messageText, boolean isSenderAgent, int senderId) {
        this.ticketId = ticketId;
        this.messageText = messageText;
        this.isSenderAgent = isSenderAgent;
        this.senderId = senderId;
    }
    
    // Getters and Setters
    public int getMessageId() {
        return messageId;
    }
    
    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }
    
    public int getTicketId() {
        return ticketId;
    }
    
    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }
    
    public String getMessageText() {
        return messageText;
    }
    
    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
    
    public Timestamp getSentAt() {
        return sentAt;
    }
    
    public void setSentAt(Timestamp sentAt) {
        this.sentAt = sentAt;
    }
    
    public boolean isSenderAgent() {
        return isSenderAgent;
    }
    
    public void setSenderAgent(boolean senderAgent) {
        isSenderAgent = senderAgent;
    }
    
    public int getSenderId() {
        return senderId;
    }
    
    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }
    
    public String getSenderName() {
        return senderName;
    }
    
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
    
    @Override
    public String toString() {
        return "TicketConversation{" +
                "messageId=" + messageId +
                ", ticketId=" + ticketId +
                ", messageText='" + messageText + '\'' +
                ", sentAt=" + sentAt +
                ", isSenderAgent=" + isSenderAgent +
                ", senderId=" + senderId +
                ", senderName='" + senderName + '\'' +
                '}';
    }
}