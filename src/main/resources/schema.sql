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

-- Create image table
CREATE TABLE image (
    id INT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36),
    path VARCHAR(500) NOT NULL,
    size BIGINT NOT NULL DEFAULT 0,
    content_type VARCHAR(100) NOT NULL,
    notification_id BIGINT NOT NULL,
    CONSTRAINT fk_image_notification_id 
        FOREIGN KEY (notification_id) REFERENCES notifications(id) ON DELETE CASCADE
);

-- Create indexes for performance optimization

-- Index on send_on for chronological ordering (newest first)
CREATE INDEX idx_notifications_send_on ON notifications(send_on DESC);

-- Index on created_at for administrative queries
CREATE INDEX idx_notifications_created_at ON notifications(created_at);

-- Index on from_sender for sender-based queries
CREATE INDEX idx_notifications_from_sender ON notifications(from_sender);

-- Index on notification_id for efficient image lookups
CREATE INDEX idx_image_notification_id ON image(notification_id);

-- Index on image path for efficient path-based lookups
CREATE INDEX idx_image_path ON image(path);

-- Index on image content_type for filtering by MIME type
CREATE INDEX idx_image_content_type ON image(content_type);

-- Index on image size for size-based queries
CREATE INDEX idx_image_size ON image(size);

-- Index on image uuid for UUID-based lookups (unique identifier)
CREATE INDEX idx_image_uuid ON image(uuid);

-- Composite index for notification content search (if full-text search is needed)
-- CREATE FULLTEXT INDEX idx_notifications_content_fulltext ON notifications(content);

-- Comments for table documentation
ALTER TABLE notifications COMMENT = 'Stores notification messages with metadata';
ALTER TABLE image COMMENT = 'Stores image files associated with notifications';

-- Column comments for better documentation
ALTER TABLE notifications 
    MODIFY COLUMN id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Unique identifier for notification',
    MODIFY COLUMN content TEXT NOT NULL COMMENT 'Notification message content',
    MODIFY COLUMN send_on BIGINT NOT NULL COMMENT 'Unix timestamp for when to send notification',
    MODIFY COLUMN from_sender VARCHAR(255) NOT NULL COMMENT 'Sender identifier or name',
    MODIFY COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when notification was created';

ALTER TABLE image 
    MODIFY COLUMN id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'Unique identifier for image',
    MODIFY COLUMN uuid VARCHAR(36) COMMENT 'UUID for image identification',
    MODIFY COLUMN path VARCHAR(500) NOT NULL COMMENT 'File path or storage location',
    MODIFY COLUMN size BIGINT NOT NULL DEFAULT 0 COMMENT 'File size in bytes',
    MODIFY COLUMN content_type VARCHAR(100) NOT NULL COMMENT 'MIME type of the image',
    MODIFY COLUMN notification_id BIGINT NOT NULL COMMENT 'Foreign key to notifications table';