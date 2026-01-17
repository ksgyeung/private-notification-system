# Requirements Document

## Introduction

A Java 21 Spring web application that provides a complete notification system with two primary APIs: one for creating/storing notifications and another for allowing devices to fetch notifications on-demand. The system uses MySQL database for persistent storage, requires Bearer token authentication for all endpoints, and is containerized using Docker with Docker Compose for easy deployment. The system operates on a pull-based model where client devices fetch notifications when needed rather than receiving push notifications.

## Glossary

- **Notification_System**: The Java 21 Spring web application that manages notifications
- **Notification**: A message or alert containing content, optional images, sender information, and scheduling details
- **Device**: Any client application, mobile device, or service that can fetch notifications
- **Issuer**: A service or application that creates and stores notifications through the system
- **Recipient**: The target entity (user, device, or service) for whom a notification is intended
- **MySQL_Database**: The MySQL database system used for persistent notification storage
- **Bearer_Token**: An authentication token passed in the Authorization header for API access
- **Docker_Container**: A containerized instance of the notification system application
- **Docker_Compose**: The orchestration tool used to manage the application and database containers
- **Image_Storage_Folder**: A file system directory where notification images are stored, configured in application.yaml

## Requirements

### Requirement 1: Authentication

**User Story:** As a system administrator, I want all API endpoints to require authentication, so that only authorized clients can access the notification system.

#### Acceptance Criteria

1. WHEN any API endpoint is called without an Authorization header, THE Notification_System SHALL return HTTP 401 Unauthorized
2. WHEN an API endpoint is called with an invalid Bearer token, THE Notification_System SHALL return HTTP 401 Unauthorized with error details
3. WHEN an API endpoint is called with a valid Bearer token, THE Notification_System SHALL process the request normally
4. THE Notification_System SHALL validate Bearer tokens in the format "Authorization: Bearer XXXXXXXX"
5. THE Notification_System SHALL apply authentication checks to all notification creation and retrieval endpoints

### Requirement 2: Notification Creation

**User Story:** As an issuer service, I want to create and store notifications through an API, so that they can be retrieved later by client devices.

#### Acceptance Criteria

1. WHEN an issuer sends a valid notification request, THE Notification_System SHALL create and store a new notification with a unique identifier
2. WHEN creating a notification, THE Notification_System SHALL require content, sender information (from field), and send timestamp
3. WHERE images are provided, THE Notification_System SHALL store the image files in the configured Image_Storage_Folder and save the filenames in the database
4. WHEN a notification is created, THE Notification_System SHALL store it with the provided send_on timestamp (Unix epoch)
5. WHEN an invalid notification request is received, THE Notification_System SHALL return a descriptive error message

### Requirement 3: Notification Retrieval

**User Story:** As a device or client application, I want to fetch notifications through an API on-demand by sending JSON parameters, so that I can check for and display new notifications when needed.

#### Acceptance Criteria

1. WHEN a device sends a POST request with JSON data to /notification/retrieve, THE Notification_System SHALL return stored notifications based on the provided parameters
2. WHEN a device requests notifications, THE Notification_System SHALL return them in chronological order (newest first based on send_on timestamp)
3. WHEN a device fetches notifications, THE Notification_System SHALL include notification ID, content, images array, send_on timestamp, and from field
4. WHEN no notifications match the request parameters, THE Notification_System SHALL return an empty list with success status
5. WHERE pagination parameters are provided in the JSON request, THE Notification_System SHALL support limiting results and providing offset-based navigation

### Requirement 4: Database Schema

**User Story:** As a system administrator, I want a properly structured MySQL database schema, so that notifications are stored efficiently and reliably.

#### Acceptance Criteria

1. THE Notification_System SHALL use a MySQL database table with id, content, images, send_on, and from fields
2. WHEN the database is initialized, THE Notification_System SHALL create the notifications table using a provided DDL script
3. THE Notification_System SHALL store the id field as a primary key with auto-increment
4. THE Notification_System SHALL store the images field as a JSON array of image filenames
5. THE Notification_System SHALL store the send_on field as a Unix epoch timestamp

### Requirement 5: Data Persistence

**User Story:** As a system administrator, I want notifications to be persistently stored using Spring JPA, so that they remain available across application restarts and provide reliable CRUD operations.

#### Acceptance Criteria

1. WHEN notifications are created, THE Notification_System SHALL persist them to the MySQL database using Spring JPA
2. WHEN the application restarts, THE Notification_System SHALL maintain all previously stored notifications through JPA entity management
3. WHEN storing notifications, THE Notification_System SHALL ensure data integrity and consistency using JPA transactions
4. THE Notification_System SHALL use Spring JPA repositories for all CRUD operations on notifications
5. THE Notification_System SHALL use appropriate JPA annotations for entity mapping and database relationships

### Requirement 6: API Response Format

**User Story:** As a client developer, I want consistent API response formats and clear endpoint definitions, so that I can reliably integrate with the notification system.

#### Acceptance Criteria

1. THE Notification_System SHALL provide a POST /notification/create endpoint for creating notifications with JSON request body
2. THE Notification_System SHALL provide a POST /notification/retrieve endpoint for fetching notifications with JSON request body
3. THE Notification_System SHALL accept and return responses in JSON format for all API endpoints
4. WHEN operations succeed, THE Notification_System SHALL return appropriate HTTP status codes (200, 201)
5. WHEN errors occur, THE Notification_System SHALL return appropriate HTTP error codes (400, 401, 404, 500)
6. WHEN returning notifications, THE Notification_System SHALL include all required fields in a consistent JSON structure
7. THE Notification_System SHALL include error details in error responses to aid debugging

### Requirement 7: Health Check Endpoint

**User Story:** As a DevOps engineer, I want a health check endpoint for Docker container monitoring, so that I can verify the application is running properly.

#### Acceptance Criteria

1. THE Notification_System SHALL provide a health check endpoint that returns "ok" as plain text
2. WHEN the health check endpoint is accessed, THE Notification_System SHALL return HTTP 200 status code
3. THE health check endpoint SHALL NOT require authentication
4. THE health check endpoint SHALL be accessible for Docker health check configuration
5. THE health check endpoint SHALL respond quickly to enable frequent monitoring

### Requirement 8: Input Validation

**User Story:** As a system administrator, I want robust input validation, so that the system remains secure and stable.

#### Acceptance Criteria

1. WHEN notification content exceeds maximum length, THE Notification_System SHALL reject the request with validation error
2. WHEN required fields (content, from) are missing from requests, THE Notification_System SHALL return specific validation errors
3. WHEN invalid image filenames are provided, THE Notification_System SHALL validate and reject malformed entries
4. THE Notification_System SHALL sanitize input data to prevent SQL injection attacks
5. WHEN send_on timestamps are invalid or malformed, THE Notification_System SHALL return appropriate validation errors

### Requirement 9: DDL Script Generation

**User Story:** As a database administrator, I want a DDL script to create the notification table, so that I can set up the database schema correctly.

#### Acceptance Criteria

1. THE Notification_System SHALL provide a DDL script that creates the notifications table with proper field types
2. THE DDL script SHALL define the id field as an auto-incrementing primary key
3. THE DDL script SHALL define the content field as TEXT to support long messages
4. THE DDL script SHALL define the images field as JSON type to store image filename arrays
5. THE DDL script SHALL define the send_on field as BIGINT to store Unix epoch timestamps
6. THE DDL script SHALL define the from field as VARCHAR to store sender information

### Requirement 10: Image Storage Configuration

**User Story:** As a system administrator, I want image storage to be configurable through application.yaml, so that I can control where notification images are stored.

#### Acceptance Criteria

1. THE Notification_System SHALL read the image storage folder path from application.yaml configuration
2. WHEN images are uploaded with notifications, THE Notification_System SHALL save them to the configured Image_Storage_Folder
3. THE Notification_System SHALL create the Image_Storage_Folder if it does not exist
4. WHEN serving images, THE Notification_System SHALL read them from the configured Image_Storage_Folder
5. THE application.yaml configuration SHALL include a property for the image storage folder path

### Requirement 11: Containerization and Deployment

**User Story:** As a DevOps engineer, I want the application to be containerized with Docker and orchestrated with Docker Compose, so that it can be easily deployed and managed in any environment.

#### Acceptance Criteria

1. THE Notification_System SHALL be packaged as a Docker container with all necessary dependencies
2. THE Notification_System SHALL provide a Dockerfile that builds the Java 21 Spring application
3. THE Notification_System SHALL include a Docker Compose configuration that orchestrates both the application and MySQL database
4. WHEN using Docker Compose, THE Notification_System SHALL automatically connect to the MySQL database container
5. THE Docker_Compose configuration SHALL include proper networking, volume mounting, and environment variable management
6. THE Notification_System SHALL expose the appropriate ports for external API access through Docker