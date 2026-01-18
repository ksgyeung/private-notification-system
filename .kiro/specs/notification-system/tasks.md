# Implementation Plan: Notification System

## Overview

This implementation plan reflects the current notification system implementation - a Java 21 Spring Boot application with multipart image upload, WebP conversion, UUID-based storage, cursor-based pagination, and separate image serving endpoint. The tasks document the actual implementation that has been completed, including the separate image table, StandardResponseDto wrapper, and advanced image processing capabilities.

## Tasks

- [x] 1. Set up project structure and dependencies
  - Create Spring Boot project with Java 21
  - Add dependencies: Spring Web, Spring JPA, MySQL Connector, Spring Security, WebP image processing, Apache Commons IO
  - Configure application.yaml with database, image storage, and host URL settings
  - Create directory structure for controllers, services, repositories, entities, DTOs
  - _Requirements: 6.1, 11.1, 12.1_

- [x] 2. Create database entities and schema with separate image table
  - [x] 2.1 Create Notification JPA entity
    - Implement Notification entity with id, content, sendOn, from, createdAt fields
    - Add @OneToMany relationship to Image entities
    - _Requirements: 4.2, 6.1_
  
  - [x] 2.2 Create Image JPA entity
    - Implement Image entity with id, uuid, path, size, contentType, notification relationship
    - Add @ManyToOne relationship to Notification entity
    - _Requirements: 4.3, 6.2_
  
  - [x] 2.3 Create DDL scripts for database schema
    - Write SQL DDL scripts to create notifications and image tables
    - Include proper indexes, foreign keys, and constraints
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

- [x] 3. Implement repository layer with UUID support
  - [x] 3.1 Create NotificationRepository interface
    - Extend JpaRepository with cursor-based pagination methods
    - Add findByIdGreaterThenIdOrderByIdDesc method for cursor pagination
    - _Requirements: 6.5, 3.2_
  
  - [x] 3.2 Create ImageRepository interface
    - Extend JpaRepository with UUID-based lookup methods
    - Add findByUuid, findByNotificationId, and existence check methods
    - _Requirements: 6.5_

- [x] 4. Create DTOs with StandardResponseDto wrapper
  - [x] 4.1 Create NotificationCreateDto
    - Add fields: content, from, imageUuids (List<String>)
    - Add validation annotations for required fields and size constraints
    - _Requirements: 2.2, 9.1, 9.2_
  
  - [x] 4.2 Create NotificationRetrieveDto
    - Add lastId field for cursor-based pagination
    - Remove offset-based pagination fields
    - _Requirements: 3.1_
  
  - [x] 4.3 Create response DTOs
    - Create NotificationResponseDto, NotificationResponseDto2, and StandardResponseDto
    - Include image URL construction in NotificationResponseDto2
    - _Requirements: 3.3, 7.4_

- [x] 5. Implement service layer with WebP conversion
  - [x] 5.1 Create ImageService with WebP processing
    - Implement image validation, WebP conversion, and UUID-based storage
    - Add methods: save, getImagePath, validateImageFile, getImageByUuid
    - Read configuration from application.yaml and validate storage directory
    - _Requirements: 2.3, 2.4, 9.5, 9.6, 11.1_
  
  - [x] 5.2 Create NotificationService with multipart support
    - Implement createNotification method with multipart file handling
    - Implement retrieveNotifications method with cursor-based pagination
    - Use automatic timestamp generation for send_on field
    - _Requirements: 2.1, 2.5, 3.1, 3.2, 6.1_

- [x] 6. Checkpoint - Ensure core functionality works
  - Verify core functionality is implemented correctly, ask the user if questions arise.

- [x] 7. Implement authentication with endpoint exclusions
  - [x] 7.1 Create BearerTokenAuthenticationFilter
    - Implement Filter to validate Authorization header for protected endpoints
    - Extract and validate Bearer tokens
    - Set authentication context for valid tokens
    - _Requirements: 1.1, 1.2, 1.3_
  
  - [x] 7.2 Configure Spring Security with exclusions
    - Create SecurityConfig to apply authentication filter
    - Exclude health check and image serving endpoints from authentication
    - Configure which endpoints require authentication
    - _Requirements: 1.5, 1.6, 8.1_

- [x] 8. Create REST controllers with multipart and image serving
  - [x] 8.1 Create NotificationController with multipart support
    - Implement POST /notification/create endpoint with multipart form data
    - Implement POST /notification/retrieve endpoint with cursor pagination
    - Add proper validation and StandardResponseDto wrapping
    - _Requirements: 7.1, 7.2, 7.4, 7.5_
  
  - [x] 8.2 Create ImageController for UUID-based serving
    - Implement GET /image/{uuid} endpoint for serving images by UUID
    - Set proper content-type and content-length headers
    - Handle 404 errors for non-existent UUIDs
    - _Requirements: 5.1, 5.2, 5.3_
  
  - [x] 8.3 Create HealthController
    - Implement GET /healthcheck endpoint returning "ok"
    - Ensure no authentication required
    - _Requirements: 8.1, 8.2_

- [x] 9. Add comprehensive error handling and validation
  - [x] 9.1 Create global exception handler
    - Handle validation errors, authentication errors, system errors
    - Return consistent StandardResponseDto error responses
    - _Requirements: 7.6, 7.7_
  
  - [x] 9.2 Add content length and image validation
    - Implement 10000 character limit for notification content
    - Add image size and format validation
    - Return appropriate validation errors
    - _Requirements: 9.1, 9.3, 9.4_

- [x] 10. Create Docker configuration with WebP support
  - [x] 10.1 Create Dockerfile with image processing libraries
    - Multi-stage build for Java 21 Spring Boot application
    - Include WebP processing libraries and dependencies
    - _Requirements: 12.1, 12.2_
  
  - [x] 10.2 Create docker-compose.yml with volume mounting
    - Configure application and MySQL database containers
    - Set up networking, image storage volumes, and environment variables
    - Include health check configuration using /healthcheck endpoint
    - _Requirements: 12.3, 12.4, 12.5, 12.6_

- [x] 11. Final integration verification
  - [x] 11.1 Verify end-to-end API workflows
    - Test notification creation with multipart image upload
    - Verify cursor-based pagination and image URL construction
    - Verify image serving by UUID and WebP conversion
    - _Requirements: 2.1, 3.1, 5.1, 6.1_

- [x] 12. Final checkpoint - Ensure all functionality works
  - Verify all endpoints work correctly, ask the user if questions arise.

## Notes

- All tasks have been completed and reflect the current implementation
- The system uses cursor-based pagination instead of offset-based
- Images are automatically converted to WebP format with UUID filenames
- StandardResponseDto wrapper is used for all API responses
- Separate image table with foreign key relationships
- Authentication excludes health check and image serving endpoints