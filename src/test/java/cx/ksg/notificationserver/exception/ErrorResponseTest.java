package cx.ksg.notificationserver.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ErrorResponse class.
 * 
 * Tests the error response structure and JSON serialization
 * to ensure consistent error format across the application.
 */
class ErrorResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void constructor_ShouldCreateErrorResponseWithAllFields() {
        // Arrange
        String code = "VALIDATION_ERROR";
        String message = "Request validation failed";
        String path = "/notification/create";

        // Act
        ErrorResponse errorResponse = new ErrorResponse(code, message, path);

        // Assert
        assertNotNull(errorResponse.getError());
        assertEquals(code, errorResponse.getError().getCode());
        assertEquals(message, errorResponse.getError().getMessage());
        assertEquals(path, errorResponse.getError().getPath());
        assertNotNull(errorResponse.getError().getTimestamp());
    }

    @Test
    void constructor_ShouldCreateErrorResponseWithNullPath() {
        // Arrange
        String code = "SYSTEM_ERROR";
        String message = "Database connection failed";
        String path = null;

        // Act
        ErrorResponse errorResponse = new ErrorResponse(code, message, path);

        // Assert
        assertNotNull(errorResponse.getError());
        assertEquals(code, errorResponse.getError().getCode());
        assertEquals(message, errorResponse.getError().getMessage());
        assertNull(errorResponse.getError().getPath());
        assertNotNull(errorResponse.getError().getTimestamp());
    }

    @Test
    void defaultConstructor_ShouldCreateEmptyErrorResponse() {
        // Act
        ErrorResponse errorResponse = new ErrorResponse();

        // Assert
        assertNull(errorResponse.getError());
    }

    @Test
    void errorDetails_ShouldHaveTimestampInIsoFormat() {
        // Arrange
        ErrorResponse errorResponse = new ErrorResponse("TEST_ERROR", "Test message", "/test");

        // Act
        String timestamp = errorResponse.getError().getTimestamp();

        // Assert
        assertNotNull(timestamp);
        // Verify timestamp format (should be ISO-8601)
        assertTrue(timestamp.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*Z"));
    }

    @Test
    void jsonSerialization_ShouldProduceCorrectStructure() throws Exception {
        // Arrange
        ErrorResponse errorResponse = new ErrorResponse("JSON_ERROR", "Invalid JSON", "/api/test");

        // Act
        String json = objectMapper.writeValueAsString(errorResponse);

        // Assert
        assertTrue(json.contains("\"error\""));
        assertTrue(json.contains("\"code\":\"JSON_ERROR\""));
        assertTrue(json.contains("\"message\":\"Invalid JSON\""));
        assertTrue(json.contains("\"path\":\"/api/test\""));
        assertTrue(json.contains("\"timestamp\""));
    }

    @Test
    void jsonDeserialization_ShouldRecreateObject() throws Exception {
        // Arrange
        String json = """
            {
              "error": {
                "code": "TEST_ERROR",
                "message": "Test message",
                "timestamp": "2024-01-15T10:30:00Z",
                "path": "/test/path"
              }
            }
            """;

        // Act
        ErrorResponse errorResponse = objectMapper.readValue(json, ErrorResponse.class);

        // Assert
        assertNotNull(errorResponse.getError());
        assertEquals("TEST_ERROR", errorResponse.getError().getCode());
        assertEquals("Test message", errorResponse.getError().getMessage());
        assertEquals("2024-01-15T10:30:00Z", errorResponse.getError().getTimestamp());
        assertEquals("/test/path", errorResponse.getError().getPath());
    }

    @Test
    void errorDetails_DefaultConstructor_ShouldSetTimestamp() {
        // Act
        ErrorResponse.ErrorDetails errorDetails = new ErrorResponse.ErrorDetails();

        // Assert
        assertNotNull(errorDetails.getTimestamp());
        assertTrue(errorDetails.getTimestamp().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*Z"));
    }

    @Test
    void errorDetails_ParameterizedConstructor_ShouldSetAllFields() {
        // Arrange
        String code = "AUTH_ERROR";
        String message = "Authentication failed";
        String path = "/secure/endpoint";

        // Act
        ErrorResponse.ErrorDetails errorDetails = new ErrorResponse.ErrorDetails(code, message, path);

        // Assert
        assertEquals(code, errorDetails.getCode());
        assertEquals(message, errorDetails.getMessage());
        assertEquals(path, errorDetails.getPath());
        assertNotNull(errorDetails.getTimestamp());
    }
}