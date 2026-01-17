package cx.ksg.notificationserver.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Unit tests for the Notification entity.
 * Tests basic functionality and field mappings.
 */
class NotificationTest {

    @Test
    void testNotificationCreation() {
        // Given
        String content = "Test notification content";
        Long sendOn = 1640995200L; // Unix timestamp
        String from = "test-sender";

        // When
        Notification notification = new Notification(content, sendOn, from);

        // Then
        assertNotNull(notification);
        assertEquals(content, notification.getContent());
        assertEquals(sendOn, notification.getSendOn());
        assertEquals(from, notification.getFrom());
        assertNull(notification.getId()); // ID should be null before persistence
        assertNull(notification.getCreatedAt()); // CreatedAt should be null before persistence
    }

    @Test
    void testNotificationDefaultConstructor() {
        // When
        Notification notification = new Notification();

        // Then
        assertNotNull(notification);
        assertNull(notification.getId());
        assertNull(notification.getContent());
        assertNull(notification.getSendOn());
        assertNull(notification.getFrom());
        assertNull(notification.getCreatedAt());
        assertNull(notification.getImages());
    }

    @Test
    void testNotificationSettersAndGetters() {
        // Given
        Notification notification = new Notification();
        Long id = 1L;
        String content = "Updated content";
        Long sendOn = 1640995200L;
        String from = "updated-sender";
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        notification.setId(id);
        notification.setContent(content);
        notification.setSendOn(sendOn);
        notification.setFrom(from);
        notification.setCreatedAt(createdAt);
        notification.setImages(new ArrayList<>());

        // Then
        assertEquals(id, notification.getId());
        assertEquals(content, notification.getContent());
        assertEquals(sendOn, notification.getSendOn());
        assertEquals(from, notification.getFrom());
        assertEquals(createdAt, notification.getCreatedAt());
        assertNotNull(notification.getImages());
        assertTrue(notification.getImages().isEmpty());
    }

    @Test
    void testNotificationToString() {
        // Given
        Notification notification = new Notification("Test content", 1640995200L, "test-sender");
        notification.setId(1L);
        notification.setCreatedAt(LocalDateTime.of(2024, 1, 1, 12, 0, 0));

        // When
        String result = notification.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("content='Test content'"));
        assertTrue(result.contains("sendOn=1640995200"));
        assertTrue(result.contains("from='test-sender'"));
        assertTrue(result.contains("createdAt=2024-01-01T12:00"));
    }
}