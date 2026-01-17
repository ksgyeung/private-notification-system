package cx.ksg.notificationserver.controller;

import cx.ksg.notificationserver.dto.NotificationCreateDto;
import cx.ksg.notificationserver.dto.NotificationListResponseDto;
import cx.ksg.notificationserver.dto.NotificationResponseDto;
import cx.ksg.notificationserver.dto.NotificationRetrieveDto;
import cx.ksg.notificationserver.service.NotificationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for notification operations in the notification system.
 * 
 * This controller provides endpoints for:
 * - Creating notifications (POST /notification/create)
 * - Retrieving notifications (POST /notification/retrieve)
 * 
 * All endpoints require Bearer token authentication and accept/return JSON data.
 * The controller includes proper error handling and validation.
 * 
 * Requirements: 6.1, 6.2, 6.4, 6.5, 6.6
 */
@RestController
@RequestMapping("/notification")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Creates a new notification.
     * 
     * Accepts a JSON request body containing notification data and creates
     * a new notification in the system. Returns the created notification
     * with its assigned ID and metadata.
     * 
     * @param request NotificationCreateDto containing notification data
     * @return ResponseEntity with NotificationResponseDto and HTTP 201 Created on success
     */
    @PostMapping("/create")
    public ResponseEntity<NotificationResponseDto> createNotification(
            @Valid @RequestBody NotificationCreateDto request) {
        
        logger.info("Received notification creation request from: {}", request.getFrom());
        
        // Create the notification using the service
        NotificationResponseDto response = notificationService.createNotification(request);
        
        logger.info("Successfully created notification with ID: {}", response.getId());
        
        // Return HTTP 201 Created with the created notification
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves notifications based on filtering and pagination parameters.
     * 
     * Accepts a JSON request body containing optional filtering parameters
     * (timestamp ranges, pagination) and returns matching notifications
     * ordered by send timestamp (newest first).
     * 
     * @param request NotificationRetrieveDto containing filtering parameters
     * @return ResponseEntity with NotificationListResponseDto and HTTP 200 OK on success
     */
    @PostMapping("/retrieve")
    public ResponseEntity<NotificationListResponseDto> retrieveNotifications(
            @Valid @RequestBody NotificationRetrieveDto request) {
        
        logger.info("Received notification retrieval request: {}", request);
        
        // Retrieve notifications using the service
        NotificationListResponseDto response = notificationService.retrieveNotifications(request);
        
        logger.info("Successfully retrieved {} notifications", response.getCount());
        
        // Return HTTP 200 OK with the notification list
        return ResponseEntity.ok(response);
    }
}