package cx.ksg.notificationserver.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the NotificationImage entity.
 * Tests basic functionality, field mappings, and relationships.
 * 
 * Requirements: 4.1, 5.1
 */
class NotificationImageTest {

    @Test
    void testNotificationImageDefaultConstructor() {
        // When
        NotificationImage image = new NotificationImage();

        // Then
        assertNotNull(image);
        assertNull(image.getId());
        assertNull(image.getFilepath());
        assertNull(image.getFileSize());
        assertNull(image.getNotification());
    }

    @Test
    void testNotificationImageConstructorWithFilepathAndSize() {
        // Given
        String filepath = "/app/images/test.jpg";
        Long fileSize = 1024L;

        // When
        NotificationImage image = new NotificationImage(filepath, fileSize);

        // Then
        assertNotNull(image);
        assertEquals(filepath, image.getFilepath());
        assertEquals(fileSize, image.getFileSize());
        assertNull(image.getId());
        assertNull(image.getNotification());
    }

    @Test
    void testNotificationImageConstructorWithAllFields() {
        // Given
        String filepath = "/app/images/test.jpg";
        Long fileSize = 1024L;
        Notification notification = new Notification("Test content", 1640995200L, "sender");

        // When
        NotificationImage image = new NotificationImage(filepath, fileSize, notification);

        // Then
        assertNotNull(image);
        assertEquals(filepath, image.getFilepath());
        assertEquals(fileSize, image.getFileSize());
        assertEquals(notification, image.getNotification());
        assertNull(image.getId());
    }

    @Test
    void testNotificationImageSettersAndGetters() {
        // Given
        NotificationImage image = new NotificationImage();
        Long id = 1L;
        String filepath = "/app/images/test-image.jpg";
        Long fileSize = 1024L;
        Notification notification = new Notification("Test content", 1640995200L, "test-sender");

        // When
        image.setId(id);
        image.setFilepath(filepath);
        image.setFileSize(fileSize);
        image.setNotification(notification);

        // Then
        assertEquals(id, image.getId());
        assertEquals(filepath, image.getFilepath());
        assertEquals(fileSize, image.getFileSize());
        assertEquals(notification, image.getNotification());
    }

    @Test
    void testNotificationImageWithValidData() {
        // Given
        String filepath = "/app/images/notification-image.png";
        Long fileSize = 2048L;
        Notification notification = new Notification("Test notification", 1640995200L, "sender");

        // When
        NotificationImage image = new NotificationImage();
        image.setFilepath(filepath);
        image.setFileSize(fileSize);
        image.setNotification(notification);

        // Then
        assertNotNull(image);
        assertEquals(filepath, image.getFilepath());
        assertEquals(fileSize, image.getFileSize());
        assertEquals(notification, image.getNotification());
        assertNull(image.getId()); // ID should be null before persistence
    }

    @Test
    void testNotificationImageWithNullFileSize() {
        // Given
        String filepath = "/app/images/test.jpg";
        Notification notification = new Notification("Test", 1640995200L, "sender");

        // When
        NotificationImage image = new NotificationImage();
        image.setFilepath(filepath);
        image.setFileSize(null); // File size can be null
        image.setNotification(notification);

        // Then
        assertNotNull(image);
        assertEquals(filepath, image.getFilepath());
        assertNull(image.getFileSize());
        assertEquals(notification, image.getNotification());
    }

    @Test
    void testNotificationImageRelationshipConsistency() {
        // Given
        Notification notification = new Notification("Test content", 1640995200L, "test-sender");
        NotificationImage image1 = new NotificationImage();
        NotificationImage image2 = new NotificationImage();

        // When
        image1.setFilepath("/app/images/image1.jpg");
        image1.setFileSize(1024L);
        image1.setNotification(notification);

        image2.setFilepath("/app/images/image2.png");
        image2.setFileSize(2048L);
        image2.setNotification(notification);

        // Then
        assertEquals(notification, image1.getNotification());
        assertEquals(notification, image2.getNotification());
        // Both images should reference the same notification
        assertSame(image1.getNotification(), image2.getNotification());
    }

    @Test
    void testNotificationImageFieldValidation() {
        // Given
        NotificationImage image = new NotificationImage();

        // Test various filepath formats
        String[] validFilepaths = {
            "/app/images/test.jpg",
            "/app/images/subfolder/image.png",
            "/app/images/document.pdf",
            "relative/path/image.gif"
        };

        for (String filepath : validFilepaths) {
            // When
            image.setFilepath(filepath);

            // Then
            assertEquals(filepath, image.getFilepath());
        }
    }

    @Test
    void testNotificationImageWithLargeFileSize() {
        // Given
        NotificationImage image = new NotificationImage();
        Long largeFileSize = Long.MAX_VALUE;
        String filepath = "/app/images/large-file.jpg";

        // When
        image.setFilepath(filepath);
        image.setFileSize(largeFileSize);

        // Then
        assertEquals(filepath, image.getFilepath());
        assertEquals(largeFileSize, image.getFileSize());
    }

    @Test
    void testNotificationImageWithZeroFileSize() {
        // Given
        NotificationImage image = new NotificationImage();
        Long zeroFileSize = 0L;
        String filepath = "/app/images/empty-file.jpg";

        // When
        image.setFilepath(filepath);
        image.setFileSize(zeroFileSize);

        // Then
        assertEquals(filepath, image.getFilepath());
        assertEquals(zeroFileSize, image.getFileSize());
    }

    @Test
    void testNotificationImageEquals() {
        // Given
        NotificationImage image1 = new NotificationImage();
        image1.setId(1L);
        image1.setFilepath("/app/images/test.jpg");
        image1.setFileSize(1024L);

        NotificationImage image2 = new NotificationImage();
        image2.setId(1L);
        image2.setFilepath("/app/images/test.jpg");
        image2.setFileSize(1024L);

        NotificationImage image3 = new NotificationImage();
        image3.setId(2L);
        image3.setFilepath("/app/images/different.jpg");
        image3.setFileSize(2048L);

        // Then
        assertEquals(image1, image2);
        assertNotEquals(image1, image3);
        assertNotEquals(image1, null);
        assertNotEquals(image1, "not an image");
        assertEquals(image1, image1); // reflexive
    }

    @Test
    void testNotificationImageHashCode() {
        // Given
        NotificationImage image1 = new NotificationImage();
        image1.setId(1L);
        image1.setFilepath("/app/images/test.jpg");
        image1.setFileSize(1024L);

        NotificationImage image2 = new NotificationImage();
        image2.setId(1L);
        image2.setFilepath("/app/images/test.jpg");
        image2.setFileSize(1024L);

        // Then
        assertEquals(image1.hashCode(), image2.hashCode());
    }

    @Test
    void testNotificationImageToString() {
        // Given
        Notification notification = new Notification("Test", 1640995200L, "sender");
        notification.setId(5L);
        
        NotificationImage image = new NotificationImage();
        image.setId(1L);
        image.setFilepath("/app/images/test.jpg");
        image.setFileSize(1024L);
        image.setNotification(notification);

        // When
        String result = image.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("filepath='/app/images/test.jpg'"));
        assertTrue(result.contains("fileSize=1024"));
        assertTrue(result.contains("notificationId=5"));
    }

    @Test
    void testNotificationImageToStringWithNullNotification() {
        // Given
        NotificationImage image = new NotificationImage();
        image.setId(1L);
        image.setFilepath("/app/images/test.jpg");
        image.setFileSize(1024L);
        // notification is null

        // When
        String result = image.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("filepath='/app/images/test.jpg'"));
        assertTrue(result.contains("fileSize=1024"));
        assertTrue(result.contains("notificationId=null"));
    }
}