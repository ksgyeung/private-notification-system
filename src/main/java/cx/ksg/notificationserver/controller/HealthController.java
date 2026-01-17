package cx.ksg.notificationserver.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health check controller for Docker container monitoring.
 * Provides a simple health check endpoint that returns "ok" as plain text.
 * 
 * This endpoint is excluded from authentication requirements and is designed
 * to be used by Docker health checks and monitoring systems.
 * 
 * Requirements satisfied:
 * - 7.1: Provides health check endpoint that returns "ok" as plain text
 * - 7.2: Returns HTTP 200 status code
 * - 7.3: Does not require authentication (configured in SecurityConfig)
 * - 7.4: Accessible for Docker health check configuration
 * - 7.5: Responds quickly to enable frequent monitoring
 */
@RestController
public class HealthController {

    /**
     * Health check endpoint for Docker container monitoring.
     * 
     * @return ResponseEntity containing "ok" as plain text with HTTP 200 status
     */
    @GetMapping(value = "/healthcheck", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("ok");
    }
}