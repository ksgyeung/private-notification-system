package cx.ksg.notificationserver.controller;

import cx.ksg.notificationserver.dto.NotificationResponseDto;
import cx.ksg.notificationserver.dto.NotificationResponseDto2;
import cx.ksg.notificationserver.dto.NotificationRetrieveDto;
import cx.ksg.notificationserver.dto.StandardResponseDto;
import cx.ksg.notificationserver.service.ImageService;
import cx.ksg.notificationserver.service.NotificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ImageService imageService;

    @Value("${notification.host-url}")
    private String hostUrl;

    @PostMapping("/create")
    public ResponseEntity<StandardResponseDto<?>> createNotification(
            @Valid @RequestParam @NotBlank @Size(max = 10000, message = "Content cannot exceed 10000 characters") String content,
            @Valid @RequestParam @NotBlank String from,
            @RequestParam List<MultipartFile> images
        ) {

        for(var image : images)
        {
            if(!imageService.validateImageFile(image))
            {
                return ResponseEntity.ok(new StandardResponseDto<Void>(false, "image is not valid"));
            }
        }
        
        // Create the notification using the service
        NotificationResponseDto response = notificationService.createNotification(content, from, images);
        
        logger.info("Successfully created notification with ID: {}", response.getId());
        
        NotificationResponseDto2 response2 = NotificationResponseDto2.fromNotificationResponseDto(hostUrl, response);
        return ResponseEntity.status(HttpStatus.OK).body(new StandardResponseDto<NotificationResponseDto2>(true, response2));
    }

    @PostMapping("/retrieve")
    public ResponseEntity<StandardResponseDto<?>> retrieveNotifications(
            @Valid @RequestBody NotificationRetrieveDto request) {
        
        logger.info("Received notification retrieval request: {}", request);
        
        List<NotificationResponseDto> response = notificationService.retrieveNotifications(request);
        
        var response2 = response.stream().map(x -> NotificationResponseDto2.fromNotificationResponseDto(hostUrl, x));

        return ResponseEntity.ok(new StandardResponseDto<>(true, response2));
    }
}