# Requirements Document

## Introduction

A Java 21 Spring web application that provides a complete notification system with three primary APIs: one for creating/storing notifications with multipart image upload, another for retrieving notifications using cursor-based pagination, and a third for serving images by UUID. The system uses MySQL database with separate tables for notifications and images, requires Bearer token authentication for protected endpoints, and is containerized using Docker with Docker Compose for easy deployment. The system operates on a pull-based model where client devices fetch notifications when needed rather than receiving push notifications.

## Glossary

- **Notification_System**: The Java 21 Spring web application that manages notifications
- **Notification**: A message or alert containing content, optional images, sender information, and timestamp details
- **Device**: Any client application, mobile device, or service that can fetch notifications
- **Issuer**: A service or application that creates and stores notifications through the system
- **Recipient**: The target entity (user, device, or service) for whom a notification is intended
- **MySQL_Database**: The MySQL database system used for persistent notification and image storage
- **Bearer_Token**: An authentication token passed in the Authorization header for API access
- **Docker_Container**: A containerized instance of the notification system application
- **Docker_Compose**: The orchestration tool used to manage the application and database containers
- **Image_Storage_Folder**: A file system directory where notification images are stored as WebP files with UUID filenames
- **UUID**: Universally Unique Identifier used for image filenames and database references
- **WebP**: Modern image format used for all stored images after conversion
- **StandardResponseDto**: Wrapper object containing success status, message, and data for all API responses
- **Cursor_Pagination**: Pagination method using lastId parameter instead of offset-based pagination

## Requirements

### Requirement 1: Authentication

**User Story:** As a system administrator, I want protected API endpoints to require authentication while allowing health checks without authentication, so that only authorized clients can access the notification system while monitoring remains accessible.

#### Acceptance Criteria

1. WHEN any protected API endpoint (/notification/create, /notification/retrieve) is called without an Authorization header, THE Notification_System SHALL return HTTP 401 Unauthorized
2. WHEN a protected API endpoint is called with an invalid Bearer token, THE Notification_System SHALL return HTTP 401 Unauthorized with error details
3. WHEN a protected API endpoint is called with a valid Bearer token, THE Notification_System SHALL process the request normally
4. THE Notification_System SHALL validate Bearer tokens in the format "Authorization: Bearer XXXXXXXX"
5. WHEN the health check endpoint (/healthcheck) is accessed, THE Notification_System SHALL allow access without authentication
6. WHEN the image serving endpoint (/image/{uuid}) is accessed, THE Notification_System SHALL allow access without authentication

### Requirement 2: Notification Creation with Image Upload

**User Story:** As an issuer service, I want to create and store notifications through a multipart form API with image upload support, so that notifications with images can be stored and retrieved later by client devices.

#### Acceptance Criteria

1. WHEN an issuer sends a valid multipart form request to POST /notification/create, THE Notification_System SHALL create and store a new notification with a unique identifier
2. WHEN creating a notification, THE Notification_System SHALL require content and from fields as form parameters
3. WHEN images are provided as multipart files, THE Notification_System SHALL convert them to WebP format and store them with UUID-based filenames in the configured Image_Storage_Folder
4. WHEN images are uploaded, THE Notification_System SHALL create corresponding records in the image table with UUID, path, size, and content type
5. WHEN a notification is created, THE Notification_System SHALL automatically set the send_on timestamp to the current Unix epoch time
6. WHEN an invalid notification request is received, THE Notification_System SHALL return a StandardResponseDto with success=false and descriptive error message
7. WHEN image files exceed size limits or are invalid formats, THE Notification_System SHALL reject the request with validation errors

### Requirement 3: Notification Retrieval with Cursor Pagination

**User Story:** As a device or client application, I want to fetch notifications through a cursor-based pagination API, so that I can efficiently retrieve notifications in chronological order without missing updates.

#### Acceptance Criteria

1. WHEN a device sends a POST request with JSON data to /notification/retrieve, THE Notification_System SHALL return stored notifications based on cursor pagination using lastId parameter
2. WHEN a device requests notifications, THE Notification_System SHALL return them in chronological order (newest first based on ID)
3. WHEN a device fetches notifications, THE Notification_System SHALL return NotificationResponseDto2 objects with image URLs instead of filenames
4. WHEN no notifications match the request parameters, THE Notification_System SHALL return an empty list wrapped in StandardResponseDto with success=true
5. WHEN pagination is used, THE Notification_System SHALL return up to 50 notifications per request using cursor-based pagination with lastId parameter
6. WHEN image URLs are returned, THE Notification_System SHALL construct full URLs using the configured host URL and /image/{uuid} endpoint

### Requirement 4: Database Schema with Separate Image Table

**User Story:** As a system administrator, I want a properly structured MySQL database schema with separate tables for notifications and images, so that data is stored efficiently and relationships are maintained properly.

#### Acceptance Criteria

1. THE Notification_System SHALL use a MySQL database with separate notifications and image tables
2. THE notifications table SHALL have id (auto-increment primary key), content (TEXT), send_on (BIGINT), from_sender (VARCHAR), and created_at (TIMESTAMP) fields
3. THE image table SHALL have id (auto-increment primary key), uuid (VARCHAR), path (VARCHAR), size (BIGINT), content_type (VARCHAR), and notification_id (foreign key) fields
4. WHEN the database is initialized, THE Notification_System SHALL create both tables using proper DDL scripts with indexes and foreign key constraints
5. THE image table SHALL have a foreign key relationship to the notifications table with cascade delete

### Requirement 5: Image Serving Endpoint

**User Story:** As a client application, I want to retrieve images by UUID through a dedicated endpoint, so that I can display notification images to users.

#### Acceptance Criteria

1. THE Notification_System SHALL provide a GET /image/{uuid} endpoint for serving images by UUID
2. WHEN a valid UUID is provided, THE Notification_System SHALL return the corresponding image file with proper content type headers
3. WHEN an invalid or non-existent UUID is provided, THE Notification_System SHALL return HTTP 404 Not Found
4. THE image serving endpoint SHALL NOT require authentication to allow easy embedding in client applications
5. WHEN serving images, THE Notification_System SHALL set appropriate content-type and content-length headers based on stored image metadata

### Requirement 6: Data Persistence with JPA

**User Story:** As a system administrator, I want notifications and images to be persistently stored using Spring JPA with proper entity relationships, so that they remain available across application restarts and provide reliable CRUD operations.

#### Acceptance Criteria

1. WHEN notifications are created, THE Notification_System SHALL persist them to the MySQL database using Spring JPA with @Entity annotations
2. WHEN images are uploaded, THE Notification_System SHALL persist image metadata to the image table with proper @ManyToOne relationship to notifications
3. WHEN the application restarts, THE Notification_System SHALL maintain all previously stored notifications and images through JPA entity management
4. WHEN storing data, THE Notification_System SHALL ensure data integrity and consistency using @Transactional annotations
5. THE Notification_System SHALL use Spring JPA repositories for all CRUD operations on both notifications and images
6. THE Notification_System SHALL use appropriate JPA annotations for entity mapping, relationships, and database constraints

### Requirement 7: API Response Format with StandardResponseDto

**User Story:** As a client developer, I want consistent API response formats using StandardResponseDto wrapper and clear endpoint definitions, so that I can reliably integrate with the notification system.

#### Acceptance Criteria

1. THE Notification_System SHALL provide a POST /notification/create endpoint accepting multipart form data with content, from, and images parameters
2. THE Notification_System SHALL provide a POST /notification/retrieve endpoint accepting JSON request body with lastId parameter
3. THE Notification_System SHALL provide a GET /image/{uuid} endpoint for serving images directly
4. THE Notification_System SHALL wrap all API responses in StandardResponseDto containing success boolean, message string, and data object
5. WHEN operations succeed, THE Notification_System SHALL return HTTP 200 status with StandardResponseDto having success=true
6. WHEN errors occur, THE Notification_System SHALL return appropriate HTTP status codes with StandardResponseDto having success=false and descriptive message
7. WHEN returning notifications, THE Notification_System SHALL use NotificationResponseDto2 with full image URLs instead of filenames

### Requirement 8: Health Check Endpoint

**User Story:** As a DevOps engineer, I want a health check endpoint for Docker container monitoring, so that I can verify the application is running properly.

#### Acceptance Criteria

1. THE Notification_System SHALL provide a GET /healthcheck endpoint that returns "ok" as plain text
2. WHEN the health check endpoint is accessed, THE Notification_System SHALL return HTTP 200 status code
3. THE health check endpoint SHALL NOT require authentication
4. THE health check endpoint SHALL be accessible for Docker health check configuration
5. THE health check endpoint SHALL respond quickly to enable frequent monitoring

### Requirement 9: Input Validation and Image Processing

**User Story:** As a system administrator, I want robust input validation and automatic image processing, so that the system remains secure and provides consistent image formats.

#### Acceptance Criteria

1. WHEN notification content exceeds 10000 characters, THE Notification_System SHALL reject the request with validation error
2. WHEN required fields (content, from) are missing from requests, THE Notification_System SHALL return specific validation errors
3. WHEN uploaded images exceed configured size limits, THE Notification_System SHALL reject the request with validation error
4. WHEN uploaded images are not valid image formats, THE Notification_System SHALL reject the request with validation error
5. WHEN valid images are uploaded, THE Notification_System SHALL convert them to WebP format regardless of original format
6. THE Notification_System SHALL generate UUID-based filenames for all stored images to prevent conflicts

### Requirement 10: DDL Script Generation

**User Story:** As a database administrator, I want DDL scripts to create both notification and image tables, so that I can set up the database schema correctly.

#### Acceptance Criteria

1. THE Notification_System SHALL provide DDL scripts that create both notifications and image tables with proper field types
2. THE notifications table DDL SHALL define id as auto-incrementing primary key, content as TEXT, send_on as BIGINT, from_sender as VARCHAR, and created_at as TIMESTAMP
3. THE image table DDL SHALL define id as auto-incrementing primary key, uuid as VARCHAR, path as VARCHAR, size as BIGINT, content_type as VARCHAR, and notification_id as foreign key
4. THE DDL scripts SHALL include proper indexes for performance optimization
5. THE DDL scripts SHALL define foreign key constraints with cascade delete from notifications to images

### Requirement 11: Image Storage Configuration

**User Story:** As a system administrator, I want image storage and processing to be configurable through application.yaml, so that I can control where images are stored and how they are processed.

#### Acceptance Criteria

1. THE Notification_System SHALL read the image storage folder path from application.yaml configuration
2. THE Notification_System SHALL read the maximum image file size from application.yaml configuration
3. THE Notification_System SHALL read the host URL for constructing image URLs from application.yaml configuration
4. WHEN the application starts, THE Notification_System SHALL validate that the configured image storage folder exists and is writable
5. THE application.yaml configuration SHALL include properties for image storage path, max size, and host URL

### Requirement 12: Containerization and Deployment

**User Story:** As a DevOps engineer, I want the application to be containerized with Docker and orchestrated with Docker Compose, so that it can be easily deployed and managed in any environment.

#### Acceptance Criteria

1. THE Notification_System SHALL be packaged as a Docker container with all necessary dependencies including WebP image processing libraries
2. THE Notification_System SHALL provide a Dockerfile that builds the Java 21 Spring application with proper image processing support
3. THE Notification_System SHALL include a Docker Compose configuration that orchestrates both the application and MySQL database containers
4. WHEN using Docker Compose, THE Notification_System SHALL automatically connect to the MySQL database container with proper networking
5. THE Docker_Compose configuration SHALL include proper volume mounting for image storage, environment variable management, and health checks
6. THE Notification_System SHALL expose the appropriate ports for external API access through Docker