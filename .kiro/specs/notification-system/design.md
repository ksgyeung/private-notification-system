# Design Document: Notification System

## Overview

The notification system is a Java 21 Spring Boot web application that provides a pull-based notification service. The system consists of two primary REST APIs: one for creating and storing notifications, and another for retrieving notifications on-demand. The application uses MySQL for persistent storage via Spring JPA, implements Bearer token authentication for security, and is fully containerized with Docker and Docker Compose for easy deployment.

The system operates on a pull-based model where client devices fetch notifications when needed rather than receiving push notifications. Images associated with notifications are stored in the file system with configurable storage paths, while the database stores metadata and image filenames.

## Architecture

The application follows a layered Spring Boot architecture:

```
┌─────────────────────────────────────────┐
│              REST Controllers           │
│  ┌─────────────────┐ ┌─────────────────┐│
│  │ NotificationCtrl│ │  HealthCtrl     ││
│  │ /notification/* │ │  /healthcheck   ││
│  └─────────────────┘ └─────────────────┘│
└─────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────┐
│           Security Layer                │
│        Bearer Token Authentication      │
└─────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────┐
│            Service Layer                │
│  ┌─────────────────┐ ┌─────────────────┐│
│  │NotificationSvc  │ │  ImageService   ││
│  └─────────────────┘ └─────────────────┘│
└─────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────┐
│         Repository Layer                │
│        Spring JPA Repositories          │
└─────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────┐
│            MySQL Database               │
│     notifications + notification_images │
└─────────────────────────────────────────┘
```

### Container Architecture

```
┌─────────────────────────────────────────┐
│           Docker Compose                │
│  ┌─────────────────┐ ┌─────────────────┐│
│  │   Spring App    │ │  MySQL Database ││
│  │   Container     │ │   Container     ││
│  │   Port: 8080    │ │   Port: 3306    ││
│  └─────────────────┘ └─────────────────┘│
│           │                   │         │
│  ┌─────────────────┐ ┌─────────────────┐│
│  │  Image Storage  │ │   DB Volume     ││
│  │    Volume       │ │    Volume       ││
│  └─────────────────┘ └─────────────────┘│
└─────────────────────────────────────────┘
```

## Components and Interfaces

### REST Controllers

#### NotificationController
- **POST /notification/create**: Creates and stores new notifications
- **POST /notification/retrieve**: Retrieves notifications based on JSON parameters
- Both endpoints require Bearer token authentication
- Accept and return JSON data

#### HealthController
- **GET /healthcheck**: Returns "ok" for Docker health checks
- No authentication required
- Returns plain text response

### Service Layer

#### NotificationService
```java
@Service
public class NotificationService {
    NotificationResponseDto createNotification(NotificationCreateDto request);
    NotificationListResponseDto retrieveNotifications(NotificationRetrieveDto request);
}
```

#### ImageService
```java
@Service
public class ImageService {
    List<String> saveImages(List<MultipartFile> images);
    String getImagePath(String filename);
    boolean validateImageFile(MultipartFile file);
}
```

### Repository Layer

#### NotificationRepository
```java
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByOrderBySendOnDesc();
    Page<Notification> findAllByOrderBySendOnDesc(Pageable pageable);
}
```

#### NotificationImageRepository
```java
@Repository
public interface NotificationImageRepository extends JpaRepository<NotificationImage, Long> {
    List<NotificationImage> findByNotificationId(Long notificationId);
    void deleteByNotificationId(Long notificationId);
}
```

### Security Configuration

#### AuthenticationFilter
```java
@Component
public class BearerTokenAuthenticationFilter implements Filter {
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain);
    boolean validateBearerToken(String token);
}
```

## Data Models

### Notification Entity
```java
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "send_on")
    private Long sendOn; // Unix epoch timestamp
    
    @Column(name = "from_sender")
    private String from;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NotificationImage> images;
}
```

### NotificationImage Entity
```java
@Entity
@Table(name = "notification_images")
public class NotificationImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "filepath", nullable = false)
    private String filepath;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;
}
```

### DTOs

#### NotificationCreateDto
```java
public class NotificationCreateDto {
    @NotBlank
    private String content;
    
    @NotNull
    private Long sendOn;
    
    @NotBlank
    private String from;
    
    private List<String> images; // Optional image filenames
}
```

#### NotificationRetrieveDto
```java
public class NotificationRetrieveDto {
    private Integer limit;
    private Integer offset;
    private Long fromTimestamp;
    private Long toTimestamp;
}
```

#### NotificationResponseDto
```java
public class NotificationResponseDto {
    private Long id;
    private String content;
    private List<String> images;
    private Long sendOn;
    private String from;
}
```

### Database Schema

The MySQL database contains two related tables:

#### Notifications Table
```sql
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    send_on BIGINT NOT NULL,
    from_sender VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_send_on ON notifications(send_on DESC);
```

#### Notification Images Table
```sql
CREATE TABLE notification_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notification_id BIGINT NOT NULL,
    filepath VARCHAR(500) NOT NULL,
    file_size BIGINT,
    FOREIGN KEY (notification_id) REFERENCES notifications(id) ON DELETE CASCADE
);

CREATE INDEX idx_notification_id ON notification_images(notification_id);
```

### Configuration

#### application.yaml
```yaml
spring:
  datasource:
    url: jdbc:mysql://mysql:3306/notifications
    username: ${DB_USERNAME:notif_user}
    password: ${DB_PASSWORD:notif_pass}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect

notification:
  image-storage-path: ${IMAGE_STORAGE_PATH:/app/images}
  auth:
    bearer-token: ${BEARER_TOKEN:default-token}

server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

Based on the prework analysis and property reflection, the following properties ensure system correctness:

### Property 1: Authentication enforcement
*For any* API request to protected endpoints (/notification/create, /notification/retrieve), the request should be rejected with HTTP 401 if it lacks a valid Bearer token, and should be processed normally if it contains a valid Bearer token
**Validates: Requirements 1.1, 1.2, 1.3**

### Property 2: Notification creation with unique identifiers
*For any* valid notification creation request, the system should create a notification with a unique ID that doesn't conflict with existing notifications
**Validates: Requirements 2.1**

### Property 3: Required field validation
*For any* notification creation request missing required fields (content, from, sendOn), the system should reject the request with specific validation errors
**Validates: Requirements 2.2, 8.2**

### Property 4: Image storage consistency
*For any* notification created with images, the image files should be stored in the configured folder and the filenames should be saved in the database notification record
**Validates: Requirements 2.3, 10.2**

### Property 5: Timestamp preservation
*For any* notification creation request, the send_on timestamp provided should be stored exactly as provided in the database
**Validates: Requirements 2.4**

### Property 6: Parameterized retrieval
*For any* notification retrieve request with filtering parameters, all returned notifications should match the specified criteria (timestamp ranges, limits, offsets)
**Validates: Requirements 3.1**

### Property 7: Chronological ordering
*For any* notification retrieve request, the returned notifications should be ordered by send_on timestamp in descending order (newest first)
**Validates: Requirements 3.2**

### Property 8: Response completeness
*For any* notification in a retrieve response, the notification should include all required fields: id, content, images array, send_on timestamp, and from field
**Validates: Requirements 3.3**

### Property 9: Persistence round-trip
*For any* notification created through the API, querying the database should return the same notification data that was originally submitted
**Validates: Requirements 5.1**

### Property 10: Content length validation
*For any* notification creation request with content exceeding the maximum allowed length, the system should reject the request with a validation error
**Validates: Requirements 8.1**

## Error Handling

The system implements comprehensive error handling across all layers:

### Authentication Errors
- **401 Unauthorized**: Missing or invalid Bearer token
- **403 Forbidden**: Valid token but insufficient permissions (future extension)

### Validation Errors
- **400 Bad Request**: Missing required fields, invalid data formats, content too long
- **422 Unprocessable Entity**: Valid JSON but business rule violations

### System Errors
- **500 Internal Server Error**: Database connection issues, file system errors
- **503 Service Unavailable**: System overload or maintenance mode

### Error Response Format
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Required field 'content' is missing",
    "timestamp": "2024-01-15T10:30:00Z",
    "path": "/notification/create"
  }
}
```

## Testing Strategy

The notification system employs a dual testing approach combining unit tests and property-based tests for comprehensive coverage.

### Property-Based Testing
Property-based tests validate universal properties across many generated inputs using a Java property testing library such as jqwik or QuickTheories. Each property test runs a minimum of 100 iterations with randomized inputs.

**Property Test Configuration:**
- Minimum 100 iterations per property test
- Each test references its corresponding design document property
- Tag format: **Feature: notification-system, Property {number}: {property_text}**

**Key Property Tests:**
- Authentication enforcement across all protected endpoints
- Notification creation with unique ID generation
- Required field validation with various missing field combinations
- Image storage and database consistency
- Timestamp preservation across different epoch values
- Chronological ordering with random timestamp sequences
- Response completeness verification
- Content length boundary testing

### Unit Testing
Unit tests focus on specific examples, edge cases, and integration points:

**Controller Layer Tests:**
- Endpoint existence and HTTP method validation
- JSON request/response format verification
- Health check endpoint returns "ok"

**Service Layer Tests:**
- Business logic validation
- Image file handling edge cases
- Database transaction rollback scenarios

**Repository Layer Tests:**
- JPA query correctness
- Database constraint validation
- Connection pooling behavior

**Integration Tests:**
- End-to-end API workflows
- Docker container health checks
- Database schema validation
- Configuration loading verification

### Test Data Management
- Use test containers for isolated database testing
- Mock external dependencies (file system, authentication service)
- Generate realistic test data with proper constraints
- Clean up test artifacts between test runs

The combination of property-based tests (covering broad input spaces) and unit tests (covering specific scenarios) ensures both functional correctness and edge case handling.