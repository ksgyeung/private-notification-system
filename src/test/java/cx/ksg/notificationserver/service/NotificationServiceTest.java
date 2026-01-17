package cx.ksg.notificationserver.service;

import cx.ksg.notificationserver.dto.NotificationCreateDto;
import cx.ksg.notificationserver.dto.NotificationListResponseDto;
import cx.ksg.notificationserver.dto.NotificationResponseDto;
import cx.ksg.notificationserver.dto.NotificationRetrieveDto;
import cx.ksg.notificationserver.entity.Notification;
import cx.ksg.notificationserver.entity.NotificationImage;
import cx.ksg.notificationserver.repository.NotificationImageRepository;
import cx.ksg.notificationserver.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationService.
 * 
 * Tests the core functionality of creating and retrieving notifications,
 * including image handling, filtering, and pagination.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationImageRepository notificationImageRepository;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private NotificationService notificationService;

    private NotificationCreateDto createDto;
    private Notification savedNotification;
    private NotificationRetrieveDto retrieveDto;

    @BeforeEach
    void setUp() {
        // Setup test data
        createDto = new NotificationCreateDto(
            "Test notification content",
            1642694400L, // Unix timestamp
            "test-sender",
            Arrays.asList("image1.jpg", "image2.png")
        );

        savedNotification = new Notification(
            "Test notification content",
            1642694400L,
            "test-sender"
        );
        savedNotification.setId(1L);

        retrieveDto = new NotificationRetrieveDto();
    }

    @Test
    void createNotification_WithValidData_ShouldReturnResponseDto() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);
        when(notificationImageRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        // Act
        NotificationResponseDto result = notificationService.createNotification(createDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test notification content", result.getContent());
        assertEquals(1642694400L, result.getSendOn());
        assertEquals("test-sender", result.getFrom());
        assertEquals(2, result.getImages().size());
        assertTrue(result.getImages().contains("image1.jpg"));
        assertTrue(result.getImages().contains("image2.png"));

        // Verify repository interactions
        verify(notificationRepository).save(any(Notification.class));
        verify(notificationImageRepository).saveAll(anyList());
    }

    @Test
    void createNotification_WithoutImages_ShouldReturnResponseDto() {
        // Arrange
        NotificationCreateDto dtoWithoutImages = new NotificationCreateDto(
            "Test content", 1642694400L, "sender", null
        );
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        // Act
        NotificationResponseDto result = notificationService.createNotification(dtoWithoutImages);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertTrue(result.getImages().isEmpty());

        // Verify no image operations
        verify(notificationImageRepository, never()).saveAll(anyList());
    }

    @Test
    void retrieveNotifications_WithoutFiltering_ShouldReturnAllNotifications() {
        // Arrange
        List<Notification> notifications = Arrays.asList(savedNotification);
        when(notificationRepository.findAllByOrderBySendOnDesc()).thenReturn(notifications);
        when(notificationImageRepository.findByNotificationIdOrderById(1L))
            .thenReturn(Collections.emptyList());

        // Act
        NotificationListResponseDto result = notificationService.retrieveNotifications(retrieveDto);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getNotifications().size());
        assertEquals(1, result.getCount());
        assertNull(result.getTotalCount()); // No pagination metadata

        NotificationResponseDto notification = result.getNotifications().get(0);
        assertEquals(1L, notification.getId());
        assertEquals("Test notification content", notification.getContent());
    }

    @Test
    void retrieveNotifications_WithPagination_ShouldReturnPagedResults() {
        // Arrange
        retrieveDto.setLimit(10);
        retrieveDto.setOffset(0);
        
        List<Notification> notifications = Arrays.asList(savedNotification);
        Page<Notification> page = new PageImpl<>(notifications, PageRequest.of(0, 10), 1);
        
        when(notificationRepository.findAllByOrderBySendOnDesc(any(Pageable.class))).thenReturn(page);
        when(notificationImageRepository.findByNotificationIdOrderById(1L))
            .thenReturn(Collections.emptyList());

        // Act
        NotificationListResponseDto result = notificationService.retrieveNotifications(retrieveDto);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getNotifications().size());
        assertEquals(1, result.getCount());
        assertEquals(1L, result.getTotalCount());
        assertEquals(10, result.getLimit());
        assertEquals(0, result.getOffset());
    }

    @Test
    void retrieveNotifications_WithTimestampRange_ShouldFilterByRange() {
        // Arrange
        retrieveDto.setFromTimestamp(1642694000L);
        retrieveDto.setToTimestamp(1642694800L);
        
        List<Notification> notifications = Arrays.asList(savedNotification);
        when(notificationRepository.findByTimestampRange(1642694000L, 1642694800L))
            .thenReturn(notifications);
        when(notificationImageRepository.findByNotificationIdOrderById(1L))
            .thenReturn(Collections.emptyList());

        // Act
        NotificationListResponseDto result = notificationService.retrieveNotifications(retrieveDto);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getNotifications().size());
        verify(notificationRepository).findByTimestampRange(1642694000L, 1642694800L);
    }

    @Test
    void retrieveNotifications_WithFromTimestampOnly_ShouldFilterFromTimestamp() {
        // Arrange
        retrieveDto.setFromTimestamp(1642694000L);
        
        List<Notification> notifications = Arrays.asList(savedNotification);
        when(notificationRepository.findBySendOnGreaterThanEqualOrderBySendOnDesc(1642694000L))
            .thenReturn(notifications);
        when(notificationImageRepository.findByNotificationIdOrderById(1L))
            .thenReturn(Collections.emptyList());

        // Act
        NotificationListResponseDto result = notificationService.retrieveNotifications(retrieveDto);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getNotifications().size());
        verify(notificationRepository).findBySendOnGreaterThanEqualOrderBySendOnDesc(1642694000L);
    }

    @Test
    void retrieveNotifications_WithToTimestampOnly_ShouldFilterToTimestamp() {
        // Arrange
        retrieveDto.setToTimestamp(1642694800L);
        
        List<Notification> notifications = Arrays.asList(savedNotification);
        when(notificationRepository.findBySendOnLessThanEqualOrderBySendOnDesc(1642694800L))
            .thenReturn(notifications);
        when(notificationImageRepository.findByNotificationIdOrderById(1L))
            .thenReturn(Collections.emptyList());

        // Act
        NotificationListResponseDto result = notificationService.retrieveNotifications(retrieveDto);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getNotifications().size());
        verify(notificationRepository).findBySendOnLessThanEqualOrderBySendOnDesc(1642694800L);
    }

    @Test
    void retrieveNotifications_WithImages_ShouldIncludeImageFilenames() {
        // Arrange
        List<Notification> notifications = Arrays.asList(savedNotification);
        List<NotificationImage> images = Arrays.asList(
            new NotificationImage("image1.jpg", 1024L, savedNotification),
            new NotificationImage("image2.png", 2048L, savedNotification)
        );
        
        when(notificationRepository.findAllByOrderBySendOnDesc()).thenReturn(notifications);
        when(notificationImageRepository.findByNotificationIdOrderById(1L)).thenReturn(images);

        // Act
        NotificationListResponseDto result = notificationService.retrieveNotifications(retrieveDto);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getNotifications().size());
        
        NotificationResponseDto notification = result.getNotifications().get(0);
        assertEquals(2, notification.getImages().size());
        assertTrue(notification.getImages().contains("image1.jpg"));
        assertTrue(notification.getImages().contains("image2.png"));
    }

    @Test
    void createNotification_WhenRepositoryFails_ShouldThrowRuntimeException() {
        // Arrange
        when(notificationRepository.save(any(Notification.class)))
            .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            notificationService.createNotification(createDto);
        });

        assertTrue(exception.getMessage().contains("Failed to create notification"));
    }

    @Test
    void retrieveNotifications_WhenRepositoryFails_ShouldThrowRuntimeException() {
        // Arrange
        when(notificationRepository.findAllByOrderBySendOnDesc())
            .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            notificationService.retrieveNotifications(retrieveDto);
        });

        assertTrue(exception.getMessage().contains("Failed to retrieve notifications"));
    }
}