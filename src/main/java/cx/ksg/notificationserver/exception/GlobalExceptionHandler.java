package cx.ksg.notificationserver.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.stream.Collectors;

/**
 * Global exception handler for the notification system.
 * 
 * This @ControllerAdvice class provides centralized exception handling across
 * all controllers in the application. It handles various exception types and
 * returns consistent JSON error responses with appropriate HTTP status codes.
 * 
 * Handles:
 * - MethodArgumentNotValidException (validation errors)
 * - HttpMessageNotReadableException (JSON parsing errors)
 * - AuthenticationException (authentication errors)
 * - RuntimeException (system errors)
 * - General Exception (fallback)
 * 
 * Returns consistent JSON error responses with:
 * - Error code
 * - Error message
 * - Timestamp
 * - Path (if available)
 * 
 * Requirements: 6.5, 6.7
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles validation errors from @Valid annotations on request bodies.
     * 
     * This occurs when request data fails validation constraints defined
     * in DTOs (e.g., @NotBlank, @NotNull, @Size annotations).
     * 
     * @param ex the validation exception
     * @return ResponseEntity with validation error details and HTTP 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {
        
        logger.warn("Validation error occurred: {}", ex.getMessage());
        
        // Collect all field validation errors
        String validationErrors = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        
        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            "Request validation failed: " + validationErrors,
            getCurrentRequestPath()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles bind exceptions from form data validation.
     * 
     * Similar to MethodArgumentNotValidException but for form-based validation.
     * 
     * @param ex the bind exception
     * @return ResponseEntity with validation error details and HTTP 400 Bad Request
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex) {
        
        logger.warn("Bind validation error occurred: {}", ex.getMessage());
        
        String validationErrors = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        
        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            "Request validation failed: " + validationErrors,
            getCurrentRequestPath()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles JSON parsing errors when request body cannot be parsed.
     * 
     * This occurs when the request body contains malformed JSON or
     * JSON that cannot be mapped to the expected DTO structure.
     * 
     * @param ex the JSON parsing exception
     * @return ResponseEntity with parsing error details and HTTP 400 Bad Request
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonParsingException(
            HttpMessageNotReadableException ex) {
        
        logger.warn("JSON parsing error occurred: {}", ex.getMessage());
        
        String message = "Invalid JSON format in request body";
        
        // Provide more specific error message for common JSON issues
        if (ex.getCause() instanceof JsonMappingException) {
            JsonMappingException jsonEx = (JsonMappingException) ex.getCause();
            message = "JSON mapping error: " + jsonEx.getOriginalMessage();
        } else if (ex.getMessage().contains("Required request body is missing")) {
            message = "Request body is required but was not provided";
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            "JSON_PARSING_ERROR",
            message,
            getCurrentRequestPath()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles authentication-related exceptions.
     * 
     * This covers cases where authentication fails or is missing.
     * Note: Most authentication errors are handled by the security filter,
     * but this provides a fallback for any that reach the controller layer.
     * 
     * @param ex the authentication exception
     * @return ResponseEntity with authentication error details and HTTP 401 Unauthorized
     */
    @ExceptionHandler({AuthenticationException.class, AuthenticationCredentialsNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex) {
        
        logger.warn("Authentication error occurred: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "AUTHENTICATION_ERROR",
            "Authentication failed: " + ex.getMessage(),
            getCurrentRequestPath()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handles access denied exceptions.
     * 
     * This occurs when a user is authenticated but lacks sufficient
     * permissions to access a resource.
     * 
     * @param ex the access denied exception
     * @return ResponseEntity with access denied error details and HTTP 403 Forbidden
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex) {
        
        logger.warn("Access denied error occurred: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "ACCESS_DENIED",
            "Access denied: " + ex.getMessage(),
            getCurrentRequestPath()
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Handles illegal argument exceptions.
     * 
     * This occurs when invalid arguments are passed to service methods
     * or when business logic validation fails.
     * 
     * @param ex the illegal argument exception
     * @return ResponseEntity with argument error details and HTTP 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        
        logger.warn("Illegal argument error occurred: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "INVALID_ARGUMENT",
            "Invalid request parameter: " + ex.getMessage(),
            getCurrentRequestPath()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles runtime exceptions (system errors).
     * 
     * This covers unexpected runtime errors such as database connection
     * issues, file system errors, or other system-level problems.
     * 
     * @param ex the runtime exception
     * @return ResponseEntity with system error details and HTTP 500 Internal Server Error
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        
        logger.error("Runtime error occurred", ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
            "SYSTEM_ERROR",
            "An unexpected system error occurred: " + ex.getMessage(),
            getCurrentRequestPath()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Handles all other exceptions (fallback handler).
     * 
     * This provides a safety net for any exceptions not explicitly handled
     * by other exception handlers in this class.
     * 
     * @param ex the general exception
     * @return ResponseEntity with generic error details and HTTP 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        
        logger.error("Unexpected error occurred", ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred. Please try again later.",
            getCurrentRequestPath()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Gets the current request path from the request context.
     * 
     * @return the current request path, or null if not available
     */
    private String getCurrentRequestPath() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            return request.getRequestURI();
        } catch (Exception e) {
            logger.debug("Could not determine current request path", e);
            return null;
        }
    }
}