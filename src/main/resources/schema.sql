-- DDL Script for Notification System Database Schema
-- This script creates the database tables for the notification system
-- Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6

-- Create notifications table
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    send_on BIGINT NOT NULL,
    from_sender VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create notification_images table
CREATE TABLE notification_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notification_id BIGINT NOT NULL,
    filepath VARCHAR(500) NOT NULL,
    file_size BIGINT,
    CONSTRAINT fk_notification_images_notification_id 
        FOREIGN KEY (notification_id) REFERENCES notifications(id) ON DELETE CASCADE
);

-- Create indexes for performance optimization
-- Index on send_on for chronological ordering (newest first)
CREATE INDEX idx_notifications_send_on ON notifications(send_on DESC);

-- Index on notification_id for efficient image lookups
CREATE INDEX idx_notification_images_notification_id ON notification_images(notification_id);

-- Index on created_at for administrative queries
CREATE INDEX idx_notifications_created_at ON notifications(created_at);