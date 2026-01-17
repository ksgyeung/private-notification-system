# Implementation Plan: Notification System

## Overview

This implementation plan converts the notification system design into discrete coding tasks for a Java 21 Spring Boot application. The tasks build incrementally, starting with project setup, then core entities, repositories, services, controllers, security, and finally containerization. Each task includes specific requirements references and optional testing sub-tasks.

## Tasks

- [x] 1. Set up project structure and dependencies
  - Create Spring Boot project with Java 21
  - Add dependencies: Spring Web, Spring JPA, MySQL Connector, Spring Security, Docker support
  - Configure basic application.yaml with database and image storage settings
  - Create directory structure for controllers, services, repositories, entities, DTOs
  - _Requirements: 5.1, 10.1, 11.1_

- [ ] 2. Create database entities and schema
  - [x] 2.1 Create Notification JPA entity
    - Implement Notification entity with id, content, sendOn, from, createdAt fields
    - Add JPA annotations for table mapping and relationships
    - _Requirements: 4.1, 5.1_
  
  - [x] 2.2 Create NotificationImage JPA entity
    - Implement NotificationImage entity with id, filepath, fileSize, notification relationship
    - Add @ManyToOne relationship to Notification entity
    - _Requirements: 4.1, 5.1_
  
  - [x] 2.3 Create DDL script for database schema
    - Write SQL DDL script to create notifications and notification_images tables
    - Include proper indexes, foreign keys, and constraints
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6_
  


- [ ] 3. Implement repository layer
  - [x] 3.1 Create NotificationRepository interface
    - Extend JpaRepository with custom query methods for ordering by sendOn
    - Add pagination support methods
    - _Requirements: 5.4, 3.2_
  
  - [x] 3.2 Create NotificationImageRepository interface
    - Extend JpaRepository with methods to find by notification ID
    - Add delete by notification ID method
    - _Requirements: 5.4_
  


- [ ] 4. Create DTOs and validation
  - [x] 4.1 Create NotificationCreateDto
    - Add fields: content, sendOn, from, images (List<String>)
    - Add validation annotations for required fields and constraints
    - _Requirements: 2.2, 8.1, 8.2_
  
  - [x] 4.2 Create NotificationRetrieveDto
    - Add optional fields: limit, offset, fromTimestamp, toTimestamp
    - Add validation for pagination parameters
    - _Requirements: 3.1_
  
  - [x] 4.3 Create response DTOs
    - Create NotificationResponseDto and NotificationListResponseDto
    - Include all required fields for API responses
    - _Requirements: 3.3, 6.3_
  


- [ ] 5. Implement service layer
  - [x] 5.1 Create ImageService
    - Implement image file storage to configured folder path
    - Add methods: saveImages, getImagePath, validateImageFile
    - Read image storage path from application.yaml configuration
    - _Requirements: 2.3, 10.1, 10.2, 10.3_
  
  - [x] 5.2 Create NotificationService
    - Implement createNotification method with image handling
    - Implement retrieveNotifications method with filtering and pagination
    - Use repositories for database operations
    - _Requirements: 2.1, 2.4, 3.1, 3.2, 5.1_
  


- [x] 6. Checkpoint - Ensure core functionality works
  - Verify core functionality is implemented correctly, ask the user if questions arise.

- [ ] 7. Implement authentication and security
  - [x] 7.1 Create BearerTokenAuthenticationFilter
    - Implement Filter to validate Authorization header
    - Extract and validate Bearer tokens
    - Set authentication context for valid tokens
    - _Requirements: 1.1, 1.2, 1.3_
  
  - [x] 7.2 Configure Spring Security
    - Create SecurityConfig to apply authentication filter
    - Configure which endpoints require authentication
    - Exclude health check endpoint from authentication
    - _Requirements: 1.1, 1.5, 7.1_
  


- [ ] 8. Create REST controllers
  - [x] 8.1 Create NotificationController
    - Implement POST /notification/create endpoint
    - Implement POST /notification/retrieve endpoint
    - Add proper error handling and validation
    - _Requirements: 6.1, 6.2, 6.4, 6.5, 6.6_
  
  - [x] 8.2 Create HealthController
    - Implement GET /healthcheck endpoint returning "ok"
    - Ensure no authentication required
    - _Requirements: 7.1, 7.2, 7.4_
  


- [ ] 9. Add comprehensive error handling
  - [x] 9.1 Create global exception handler
    - Handle validation errors, authentication errors, system errors
    - Return consistent JSON error responses
    - _Requirements: 6.5, 6.7_
  
  - [x] 9.2 Add content length validation
    - Implement maximum content length checking
    - Return appropriate validation errors
    - _Requirements: 8.1_
  


- [ ] 10. Create Docker configuration
  - [x] 10.1 Create Dockerfile
    - Multi-stage build for Java 21 Spring Boot application
    - Include all necessary dependencies and configurations
    - _Requirements: 11.1, 11.2_
  
  - [x] 10.2 Create docker-compose.yml
    - Configure application and MySQL database containers
    - Set up networking, volumes, and environment variables
    - Include health check configuration using /healthcheck endpoint
    - _Requirements: 11.3, 11.4, 11.5, 11.6_
  


- [ ] 11. Final integration verification
  - [ ] 11.1 Verify end-to-end API workflows
    - Test notification creation and retrieval manually
    - Verify database connectivity and data persistence
    - Verify image storage and retrieval functionality
    - _Requirements: 2.1, 3.1, 5.1, 10.2_

- [x] 12. Final checkpoint - Ensure all functionality works
  - Verify all endpoints work correctly, ask the user if questions arise.

## Notes

- Each task references specific requirements for traceability
- Manual verification ensures end-to-end functionality
- Checkpoints provide validation points during development