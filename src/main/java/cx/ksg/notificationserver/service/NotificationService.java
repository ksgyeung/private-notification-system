package cx.ksg.notificationserver.service;

import cx.ksg.notificationserver.dto.ImageDto;
import cx.ksg.notificationserver.dto.NotificationResponseDto;
import cx.ksg.notificationserver.dto.NotificationRetrieveDto;
import cx.ksg.notificationserver.entity.Image;
import cx.ksg.notificationserver.entity.Notification;
import cx.ksg.notificationserver.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ImageService imageService;

    @Transactional
    public NotificationResponseDto createNotification(String content, String from, List<MultipartFile> images) {
        logger.info("Creating notification from sender: {}", from);

        try {
            // Create and save the notification entity
            Notification notification = new Notification(
                content,
                System.currentTimeMillis() / 1000L,
                from
            );
            
            Notification savedNotification = notificationRepository.save(notification);
            logger.debug("Saved notification with ID: {}", savedNotification.getId());

            ArrayList<Image> images2 = new ArrayList<>();
            for(var image : images)
            {
                Image image2 = imageService.save(savedNotification, image);
                images2.add(image2);
            }

            // Create and return response DTO
            NotificationResponseDto response = new NotificationResponseDto(
                savedNotification.getId(),
                savedNotification.getContent(),
                images2.stream().map(ImageDto::fromImage).collect(Collectors.toList()),
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

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> retrieveNotifications(NotificationRetrieveDto request) {
        logger.info("Retrieving notifications with parameters: {}", request);
        
        List<Notification> notifications = notificationRepository.findByIdGreaterThenIdOrderByIdDesc(request.getLastId(), Limit.of(50));
        logger.info("Retrieved {} notifications", notifications.size());
        return notifications.stream().map(NotificationResponseDto::fromNotification).toList();
    }
}