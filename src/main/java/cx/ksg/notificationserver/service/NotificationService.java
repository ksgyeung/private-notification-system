package cx.ksg.notificationserver.service;

import cx.ksg.notificationserver.dto.NotificationCreateDto;
import cx.ksg.notificationserver.dto.NotificationListResponseDto;
import cx.ksg.notificationserver.dto.NotificationResponseDto;
import cx.ksg.notificationserver.dto.NotificationRetrieveDto;
import cx.ksg.notificationserver.entity.Notification;
import cx.ksg.notificationserver.entity.NotificationImage;
import cx.ksg.notificationserver.repository.NotificationImageRepository;
import cx.ksg.notificationserver.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for handling notification operations in the notification system.
 * 
 * This service provides functionality to:
 * - Create and store notifications with optional image handling
 * - Retrieve notifications with filtering and pagination support
 * - Convert between entities and DTOs
 * - Ensure proper ordering (newest first based on send_on timestamp)
 * 
 * The service uses repositories for database operations and integrates with
 * ImageService for handling image file operations.
 * 
 * Requirements: 2.1, 2.4, 3.1, 3.2, 5.1
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationImageRepository notificationImageRepository;
    private final ImageService imageService;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository,
                             NotificationImageRepository notificationImageRepository,
                             ImageService imageService) {
        this.notificationRepository = notificationRepository;
        this.notificationImageRepository = notificationImageRepository;
        this.imageService = imageService;
    }

    /**
     * Creates a new notification with optional image handling.
     * 
     * This method:
     * - Creates a new Notification entity from the DTO
     * - Saves the notification to the database
     * - Creates NotificationImage entities for any provided images
     * - Saves image entities to the database
     * - Returns a NotificationResponseDto with the created notification data
     * 
     * @param request NotificationCreateDto containing notification data
     * @return NotificationResponseDto with the created notification
     * @throws RuntimeException if database operations fail
     */
    @Transactional
    public NotificationResponseDto createNotification(NotificationCreateDto request) {
        logger.info("Creating notification from sender: {}", request.getFrom());
        
        try {
            // Create and save the notification entity
            Notification notification = new Notification(
                request.getContent(),
                request.getSendOn(),
                request.getFrom()
            );
            
            Notification savedNotification = notificationRepository.save(notification);
            logger.debug("Saved notification with ID: {}", savedNotification.getId());
            
            // Handle images if provided
            List<String> imageFilenames = new ArrayList<>();
            if (request.getImages() != null && !request.getImages().isEmpty()) {
                List<NotificationImage> imageEntities = new ArrayList<>();
                
                for (String imageFilename : request.getImages()) {
                    if (imageFilename != null && !imageFilename.trim().isEmpty()) {
                        // Create NotificationImage entity
                        // Note: We assume images are already saved by ImageService before this call
                        // The DTO contains the filenames returned by ImageService.saveImages()
                        NotificationImage imageEntity = new NotificationImage(
                            imageFilename,
                            null, // File size can be determined later if needed
                            savedNotification
                        );
                        imageEntities.add(imageEntity);
                        imageFilenames.add(imageFilename);
                    }
                }
                
                // Save all image entities
                if (!imageEntities.isEmpty()) {
                    notificationImageRepository.saveAll(imageEntities);
                    logger.debug("Saved {} images for notification ID: {}", 
                               imageEntities.size(), savedNotification.getId());
                }
            }
            
            // Create and return response DTO
            NotificationResponseDto response = new NotificationResponseDto(
                savedNotification.getId(),
                savedNotification.getContent(),
                imageFilenames,
                savedNotification.getSendOn(),
                savedNotification.getFrom()
            );
            
            logger.info("Successfully created notification with ID: {}", savedNotification.getId());
            return response;
            
        } catch (Exception e) {
            logger.error("Failed to create notification", e);
            throw new RuntimeException("Failed to create notification: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves notifications based on filtering and pagination parameters.
     * 
     * This method:
     * - Applies timestamp filtering if provided
     * - Applies pagination if limit/offset are provided
     * - Ensures results are ordered by sendOn timestamp descending (newest first)
     * - Loads associated images for each notification
     * - Returns NotificationListResponseDto with results and metadata
     * 
     * @param request NotificationRetrieveDto containing filtering and pagination parameters
     * @return NotificationListResponseDto with matching notifications
     */
    @Transactional(readOnly = true)
    public NotificationListResponseDto retrieveNotifications(NotificationRetrieveDto request) {
        logger.info("Retrieving notifications with parameters: {}", request);
        
        try {
            List<Notification> notifications;
            Long totalCount = null;
            
            // Determine if pagination is requested
            boolean usePagination = request.getLimit() != null;
            Pageable pageable = null;
            
            if (usePagination) {
                int offset = request.getOffset() != null ? request.getOffset() : 0;
                pageable = PageRequest.of(offset / request.getLimit(), request.getLimit());
            }
            
            // Apply filtering and retrieve notifications
            if (request.getFromTimestamp() != null && request.getToTimestamp() != null) {
                // Both timestamps provided - range query
                if (usePagination) {
                    Page<Notification> page = notificationRepository.findByTimestampRange(
                        request.getFromTimestamp(), request.getToTimestamp(), pageable);
                    notifications = page.getContent();
                    totalCount = page.getTotalElements();
                } else {
                    notifications = notificationRepository.findByTimestampRange(
                        request.getFromTimestamp(), request.getToTimestamp());
                }
                
            } else if (request.getFromTimestamp() != null) {
                // Only from timestamp provided
                if (usePagination) {
                    Page<Notification> page = notificationRepository
                        .findBySendOnGreaterThanEqualOrderBySendOnDesc(request.getFromTimestamp(), pageable);
                    notifications = page.getContent();
                    totalCount = page.getTotalElements();
                } else {
                    notifications = notificationRepository
                        .findBySendOnGreaterThanEqualOrderBySendOnDesc(request.getFromTimestamp());
                }
                
            } else if (request.getToTimestamp() != null) {
                // Only to timestamp provided
                if (usePagination) {
                    Page<Notification> page = notificationRepository
                        .findBySendOnLessThanEqualOrderBySendOnDesc(request.getToTimestamp(), pageable);
                    notifications = page.getContent();
                    totalCount = page.getTotalElements();
                } else {
                    notifications = notificationRepository
                        .findBySendOnLessThanEqualOrderBySendOnDesc(request.getToTimestamp());
                }
                
            } else {
                // No timestamp filtering
                if (usePagination) {
                    Page<Notification> page = notificationRepository.findAllByOrderBySendOnDesc(pageable);
                    notifications = page.getContent();
                    totalCount = page.getTotalElements();
                } else {
                    notifications = notificationRepository.findAllByOrderBySendOnDesc();
                }
            }
            
            // Convert to response DTOs
            List<NotificationResponseDto> responseDtos = notifications.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
            
            // Create response with pagination metadata
            NotificationListResponseDto response;
            if (usePagination) {
                response = new NotificationListResponseDto(
                    responseDtos, 
                    totalCount, 
                    request.getLimit(), 
                    request.getOffset()
                );
            } else {
                response = new NotificationListResponseDto(responseDtos);
            }
            
            logger.info("Retrieved {} notifications", responseDtos.size());
            return response;
            
        } catch (Exception e) {
            logger.error("Failed to retrieve notifications", e);
            throw new RuntimeException("Failed to retrieve notifications: " + e.getMessage(), e);
        }
    }

    /**
     * Converts a Notification entity to a NotificationResponseDto.
     * 
     * This method loads associated images and includes them in the response.
     * 
     * @param notification The Notification entity to convert
     * @return NotificationResponseDto with all required fields
     */
    private NotificationResponseDto convertToResponseDto(Notification notification) {
        // Load associated images
        List<NotificationImage> imageEntities = notificationImageRepository
            .findByNotificationIdOrderById(notification.getId());
        
        List<String> imageFilenames = imageEntities.stream()
            .map(NotificationImage::getFilepath)
            .collect(Collectors.toList());
        
        return new NotificationResponseDto(
            notification.getId(),
            notification.getContent(),
            imageFilenames,
            notification.getSendOn(),
            notification.getFrom()
        );
    }
}