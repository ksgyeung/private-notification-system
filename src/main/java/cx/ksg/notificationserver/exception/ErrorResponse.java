package cx.ksg.notificationserver.exception;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Standardized error response structure for the notification system.
 * 
 * This class provides a consistent format for all error responses
 * returned by the global exception handler. All API errors follow
 * the same JSON structure for client consistency.
 * 
 * JSON Structure:
 * {
 *   "error": {
 *     "code": "ERROR_CODE",
 *     "message": "Error description",
 *     "timestamp": "2024-01-15T10:30:00Z",
 *     "path": "/api/endpoint"
 *   }
 * }
 */
public class ErrorResponse {

    @JsonProperty("error")
    private ErrorDetails error;

    public ErrorResponse() {
    }

    public ErrorResponse(String code, String message, String path) {
        this.error = new ErrorDetails(code, message, path);
    }

    public ErrorDetails getError() {
        return error;
    }

    public void setError(ErrorDetails error) {
        this.error = error;
    }

    /**
     * Inner class representing the error details structure.
     */
    public static class ErrorDetails {
        
        private String code;
        private String message;
        private String timestamp;
        private String path;

        public ErrorDetails() {
            this.timestamp = Instant.now().toString();
        }

        public ErrorDetails(String code, String message, String path) {
            this.code = code;
            this.message = message;
            this.path = path;
            this.timestamp = Instant.now().toString();
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }
}