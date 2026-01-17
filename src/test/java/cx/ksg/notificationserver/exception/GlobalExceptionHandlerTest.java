package cx.ksg.notificationserver.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GlobalExceptionHandler.
 * 
 * Tests the various exception handling scenarios to ensure consistent
 * error responses and appropriate HTTP status codes are returned.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleValidationException_ShouldReturnBadRequest() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        org.springframework.validation.BindingResult bindingResult = mock(org.springframework.validation.BindingResult.class);
        FieldError fieldError = new FieldError("notificationCreateDto", "content", "Content is required");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError));
        when(ex.getMessage()).thenReturn("Validation failed");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationException(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("VALIDATION_ERROR", response.getBody().getError().getCode());
        assertTrue(response.getBody().getError().getMessage().contains("Content is required"));
        assertNotNull(response.getBody().getError().getTimestamp());
    }

    @Test
    void handleBindException_ShouldReturnBadRequest() {
        // Arrange
        BindException ex = mock(BindException.class);
        org.springframework.validation.BindingResult bindingResult = mock(org.springframework.validation.BindingResult.class);
        FieldError fieldError = new FieldError("notificationCreateDto", "from", "From field is required");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError));
        when(ex.getMessage()).thenReturn("Bind validation failed");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBindException(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("VALIDATION_ERROR", response.getBody().getError().getCode());
        assertTrue(response.getBody().getError().getMessage().contains("From field is required"));
    }

    @Test
    void handleJsonParsingException_ShouldReturnBadRequest() {
        // Arrange
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("JSON parse error");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleJsonParsingException(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("JSON_PARSING_ERROR", response.getBody().getError().getCode());
        assertEquals("Invalid JSON format in request body", response.getBody().getError().getMessage());
        assertNotNull(response.getBody().getError().getTimestamp());
    }

    @Test
    void handleJsonParsingException_WithMissingBody_ShouldReturnSpecificMessage() {
        // Arrange
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Required request body is missing");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleJsonParsingException(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("JSON_PARSING_ERROR", response.getBody().getError().getCode());
        assertEquals("Request body is required but was not provided", response.getBody().getError().getMessage());
    }

    @Test
    void handleAuthenticationException_ShouldReturnUnauthorized() {
        // Arrange
        AuthenticationCredentialsNotFoundException ex = new AuthenticationCredentialsNotFoundException("Authentication required");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAuthenticationException(ex);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("AUTHENTICATION_ERROR", response.getBody().getError().getCode());
        assertTrue(response.getBody().getError().getMessage().contains("Authentication required"));
    }

    @Test
    void handleAccessDeniedException_ShouldReturnForbidden() {
        // Arrange
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAccessDeniedException(ex);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ACCESS_DENIED", response.getBody().getError().getCode());
        assertTrue(response.getBody().getError().getMessage().contains("Access denied"));
    }

    @Test
    void handleIllegalArgumentException_ShouldReturnBadRequest() {
        // Arrange
        IllegalArgumentException ex = new IllegalArgumentException("Invalid parameter value");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_ARGUMENT", response.getBody().getError().getCode());
        assertTrue(response.getBody().getError().getMessage().contains("Invalid parameter value"));
    }

    @Test
    void handleRuntimeException_ShouldReturnInternalServerError() {
        // Arrange
        RuntimeException ex = new RuntimeException("Database connection failed");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleRuntimeException(ex);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SYSTEM_ERROR", response.getBody().getError().getCode());
        assertTrue(response.getBody().getError().getMessage().contains("Database connection failed"));
    }

    @Test
    void handleGeneralException_ShouldReturnInternalServerError() {
        // Arrange
        Exception ex = new Exception("Unexpected error");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGeneralException(ex);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getError().getCode());
        assertEquals("An unexpected error occurred. Please try again later.", response.getBody().getError().getMessage());
    }

    @Test
    void errorResponse_ShouldHaveConsistentStructure() {
        // Arrange
        RuntimeException ex = new RuntimeException("Test error");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleRuntimeException(ex);

        // Assert
        assertNotNull(response.getBody());
        ErrorResponse errorResponse = response.getBody();
        
        // Verify error structure
        assertNotNull(errorResponse.getError());
        assertNotNull(errorResponse.getError().getCode());
        assertNotNull(errorResponse.getError().getMessage());
        assertNotNull(errorResponse.getError().getTimestamp());
        
        // Verify timestamp format (should be ISO-8601)
        String timestamp = errorResponse.getError().getTimestamp();
        assertTrue(timestamp.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*Z"));
    }
}