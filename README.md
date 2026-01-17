# Notification System

A Java 21 Spring Boot web application that provides a complete notification system with REST APIs for creating and retrieving notifications.

## Features

- **Java 21** with Spring Boot 3.2.1
- **MySQL** database for persistent storage
- **Spring JPA** for data persistence
- **Spring Security** with Bearer token authentication
- **Docker** support for containerization
- **REST APIs** for notification management
- **Image storage** support with configurable paths

## Project Structure

```
src/
├── main/
│   ├── java/cx/ksg/notificationserver/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── entity/         # JPA entities
│   │   ├── exception/      # Exception handling
│   │   ├── repository/     # JPA repositories
│   │   ├── security/       # Security configuration
│   │   ├── service/        # Business logic services
│   │   └── NotificationSystemApplication.java
│   └── resources/
│       └── application.yaml # Main configuration
└── test/
    ├── java/               # Test classes
    └── resources/
        └── application-test.yaml # Test configuration
```

## Dependencies

- Spring Boot Web
- Spring Boot JPA
- Spring Boot Security
- Spring Boot Actuator
- Spring Boot Validation
- MySQL Connector
- H2 Database (for testing)

## Configuration

The application uses `application.yaml` for configuration:

- Database connection settings
- Image storage path configuration
- Bearer token authentication
- Server port and management endpoints

## Building

### Local Development

Use the Maven wrapper to build the project:

```bash
./mvnw clean compile
./mvnw clean package
./mvnw spring-boot:run
```

### Docker Deployment

The application includes Docker support with a complete docker-compose configuration:

```bash
# Build and start all services
docker compose up -d

# View logs
docker compose logs -f notification-app

# Stop all services
docker compose down

# Stop and remove volumes (WARNING: This will delete all data)
docker compose down -v
```

#### Environment Configuration

Copy the example environment file and customize as needed:

```bash
cp .env.example .env
# Edit .env file with your configuration
```

Key environment variables:
- `BEARER_TOKEN`: Authentication token for API access
- `MYSQL_ROOT_PASSWORD`: MySQL root password
- `MYSQL_USER`/`MYSQL_PASSWORD`: Application database credentials

#### Services

The docker-compose configuration includes:

- **notification-app**: Spring Boot application (port 8080)
- **mysql**: MySQL 8.0 database (port 3306)
- **Volumes**: Persistent storage for database and images
- **Health checks**: Automatic service health monitoring
- **Networking**: Isolated network for service communication

## Requirements

- Java 21 (for local development)
- Docker and Docker Compose (for containerized deployment)
- MySQL database (automatically provided in Docker setup)

## API Endpoints

- `POST /notification/create` - Create new notifications
- `POST /notification/retrieve` - Retrieve notifications
- `GET /healthcheck` - Health check endpoint

All notification endpoints require Bearer token authentication.