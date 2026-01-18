# Design Document: Notification System

## Overview

The notification system is a Java 21 Spring Boot web application that provides a pull-based notification service with advanced image handling capabilities. The system consists of three primary REST APIs: one for creating and storing notifications with multipart image upload, another for retrieving notifications using cursor-based pagination, and a third for serving images by UUID. The application uses MySQL with separate tables for notifications and images, implements Bearer token authentication for protected endpoints, converts all images to WebP format with UUID-based storage, and is fully containerized with Docker and Docker Compose for easy deployment.

The system operates on a pull-based model where client devices fetch notifications when needed rather than receiving push notifications. Images are automatically converted to WebP format and stored with UUID-based filenames to prevent conflicts and ensure consistent format. All API responses are wrapped in a StandardResponseDto for consistent client integration.

## Architecture

The application follows a layered Spring Boot architecture:

```
┌─────────────────────────────────────────┐
│              REST Controllers           │
│  ┌─────────────────┐ ┌─────────────────┐│
│  │ NotificationCtrl│ │  HealthCtrl     ││
│  │ /notification/* │ │  /healthcheck   ││
│  └─────────────────┘ └─────────────────┘│
│  ┌─────────────────┐                   ││
│  │  ImageCtrl      │                   ││
│  │  /image/{uuid}  │                   ││
│  └─────────────────┘                   ││
└─────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────┐
│           Security Layer                │
│    Bearer Token Authentication Filter   │
│    (excludes /healthcheck, /image/*)    │
└─────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────┐
│            Service Layer                │
│  ┌─────────────────┐ ┌─────────────────┐│
│  │NotificationSvc  │ │  ImageService   ││
│  │ (with WebP      │ │  (UUID-based    ││
│  │  conversion)    │ │   storage)      ││
│  └─────────────────┘ └─────────────────┘│
└─────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────┐
│         Repository Layer                │
│  ┌─────────────────┐ ┌─────────────────┐│
│  │NotificationRepo │ │  ImageRepo      ││
│  │(cursor pagination)│ │ (UUID lookup) ││
│  └─────────────────┘ └─────────────────┘│
└─────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────┐
│            MySQL Database               │
│     notifications + image tables        │
│     (separate tables with FK)           │
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
- **POST /notification/create**: Creates and stores new notifications with multipart form data (content, from, images)
- **POST /notification/retrieve**: Retrieves notifications using cursor-based pagination with JSON request body
- Both endpoints require Bearer token authentication
- Returns StandardResponseDto wrapper with success status and data

#### ImageController
- **GET /image/{uuid}**: Serves images directly by UUID with proper content-type headers
- No authentication required for easy client integration
- Returns image file directly with appropriate headers

#### HealthController
- **GET /healthcheck**: Returns "ok" for Docker health checks
- No authentication required
- Returns plain text response

### Service Layer

#### NotificationService
```java
@Service
public class NotificationService {
    @Transactional
    NotificationResponseDto createNotification(String content, String from, List<MultipartFile> images);
    
    @Transactional(readOnly = true)
    List<NotificationResponseDto> retrieveNotifications(NotificationRetrieveDto request);
}
```

#### ImageService
```java
@Service
public class ImageService {
    @Transactional
    Image save(Notification notification, MultipartFile multipartFile);
    
    String getImagePath(String filename);
    boolean validateImageFile(MultipartFile file);
    ImageDto getImageByUuid(String uuid);
}
```

### Repository Layer

#### NotificationRepository
```java
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByOrderBySendOnDesc();
    List<Notification> findByIdGreaterThenIdOrderByIdDesc(long id, Limit limit);
    Page<Notification> findAllByOrderBySendOnDesc(Pageable pageable);
}
```

#### ImageRepository
```java
@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {
    Optional<Image> findByUuid(String uuid);
    Optional<Image> findByPath(String path);
    List<Image> findByNotificationId(Long notificationId);
    boolean existsByUuid(String uuid);
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

### Image Entity
```java
@Entity(name = "image")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column
    private String uuid;
    
    @Column(nullable = false)
    private String path;
    
    @Column
    private long size;
    
    @Column(name = "content_type", nullable = false)
    private String contentType;
    
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
    
    @NotBlank
    private String from;
    
    private List<String> imageUuids; // Optional image UUIDs for reference
}
```

#### NotificationRetrieveDto
```java
public class NotificationRetrieveDto {
    private long lastId; // Cursor-based pagination parameter
}
```

#### NotificationResponseDto
```java
public class NotificationResponseDto {
    private Long id;
    private String content;
    private List<ImageDto> images;
    private Long sendOn;
    private String from;
}
```

#### NotificationResponseDto2
```java
public class NotificationResponseDto2 {
    private Long id;
    private String content;
    private List<String> images; // Full image URLs
    private Long sendOn;
    private String from;
}
```

#### StandardResponseDto
```java
public class StandardResponseDto<T> {
    private final boolean success;
    private final String message;
    private final T data;
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
CREATE INDEX idx_id_desc ON notifications(id DESC);
```

#### Image Table
```sql
CREATE TABLE image (
    id INT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) UNIQUE,
    path VARCHAR(500) NOT NULL,
    size BIGINT,
    content_type VARCHAR(100) NOT NULL,
    notification_id BIGINT NOT NULL,
    FOREIGN KEY (notification_id) REFERENCES notifications(id) ON DELETE CASCADE
);

CREATE INDEX idx_uuid ON image(uuid);
CREATE INDEX idx_notification_id ON image(notification_id);
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
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 50MB

notification:
  image:
    storage-path: ${IMAGE_STORAGE_PATH:/app/images}
    max-size: ${IMAGE_MAX_SIZE:10}  # MB
  host-url: ${HOST_URL:http://localhost:8080/image/}
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

### Property 1: Authentication enforcement for protected endpoints
*For any* request to protected endpoints (/notification/create, /notification/retrieve), the request should be rejected with HTTP 401 if it lacks a valid Bearer token, and should be processed normally if it contains a valid Bearer token
**Validates: Requirements 1.1, 1.2, 1.3**

### Property 2: Unique notification creation
*For any* valid multipart form request to create a notification, the system should create a notification with a unique ID that doesn't conflict with existing notifications
**Validates: Requirements 2.1**

### Property 3: Required field validation
*For any* notification creation request missing required fields (content, from), the system should reject the request with specific validation errors
**Validates: Requirements 2.2, 9.2**

### Property 4: Image processing and storage consistency
*For any* valid image uploaded with a notification, the system should convert it to WebP format, store it with a UUID-based filename, and create a corresponding database record with proper metadata
**Validates: Requirements 2.3, 2.4, 9.5, 9.6**

### Property 5: Automatic timestamp generation
*For any* notification creation request, the system should automatically set the send_on timestamp to the current Unix epoch time
**Validates: Requirements 2.5**

### Property 6: Image validation and rejection
*For any* image file that exceeds size limits or has invalid format, the system should reject the request with appropriate validation errors
**Validates: Requirements 2.7**

### Property 7: Cursor-based pagination
*For any* notification retrieve request with a lastId parameter, the system should return up to 50 notifications with IDs greater than the provided lastId
**Validates: Requirements 3.1**

### Property 8: Chronological ordering
*For any* notification retrieve request, the returned notifications should be ordered by ID in descending order (newest first)
**Validates: Requirements 3.2**

### Property 9: Response format with image URLs
*For any* notification in a retrieve response, the notification should be returned as NotificationResponseDto2 with full image URLs constructed using the configured host URL
**Validates: Requirements 3.3, 3.6**

### Property 10: Image serving by UUID
*For any* valid UUID provided to the /image/{uuid} endpoint, the system should return the corresponding image file with proper content-type headers, and return HTTP 404 for invalid UUIDs
**Validates: Requirements 5.2, 5.3**

### Property 11: Database persistence consistency
*For any* notification created and any image uploaded, the data should be properly persisted to the MySQL database with correct entity relationships and retrievable through JPA repositories
**Validates: Requirements 6.1, 6.2**

### Property 12: StandardResponseDto wrapper
*For any* API response from protected endpoints, the response should be wrapped in StandardResponseDto containing success boolean, message string, and data object
**Validates: Requirements 7.4**

### Property 13: Content length validation
*For any* notification creation request with content exceeding 10000 characters, the system should reject the request with a validation error
**Validates: Requirements 9.1**

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