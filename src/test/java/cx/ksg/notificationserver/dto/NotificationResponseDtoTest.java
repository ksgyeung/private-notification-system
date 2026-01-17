package cx.ksg.notificationserver.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NotificationResponseDto functionality.
 */
class NotificationResponseDtoTest {

    @Test
    void testDefaultConstructor() {
        // When
        NotificationResponseDto dto = new NotificationResponseDto();

        // Then
        assertNull(dto.getId());
        assertNull(dto.getContent());
        assertNull(dto.getImages());
        assertNull(dto.getSendOn());
        assertNull(dto.getFrom());
    }

    @Test
    void testConstructorWithAllFields() {
        // Given
        Long id = 1L;
        String content = "Test notification content";
        List<String> images = Arrays.asList("image1.jpg", "image2.png");
        Long sendOn = 1640995200L;
        String from = "test-sender";

        // When
        NotificationResponseDto dto = new NotificationResponseDto(id, content, images, sendOn, from);

        // Then
        assertEquals(id, dto.getId());
        assertEquals(content, dto.getContent());
        assertEquals(images, dto.getImages());
        assertEquals(sendOn, dto.getSendOn());
        assertEquals(from, dto.getFrom());
    }

    @Test
    void testConstructorWithNullValues() {
        // When
        NotificationResponseDto dto = new NotificationResponseDto(null, null, null, null, null);

        // Then
        assertNull(dto.getId());
        assertNull(dto.getContent());
        assertNull(dto.getImages());
        assertNull(dto.getSendOn());
        assertNull(dto.getFrom());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        NotificationResponseDto dto = new NotificationResponseDto();
        Long id = 123L;
        String content = "Updated content";
        List<String> images = Collections.singletonList("single-image.jpg");
        Long sendOn = 1641081600L;
        String from = "updated-sender";

        // When
        dto.setId(id);
        dto.setContent(content);
        dto.setImages(images);
        dto.setSendOn(sendOn);
        dto.setFrom(from);

        // Then
        assertEquals(id, dto.getId());
        assertEquals(content, dto.getContent());
        assertEquals(images, dto.getImages());
        assertEquals(sendOn, dto.getSendOn());
        assertEquals(from, dto.getFrom());
    }

    @Test
    void testWithEmptyImagesList() {
        // Given
        List<String> emptyImages = Collections.emptyList();
        NotificationResponseDto dto = new NotificationResponseDto(
                1L, "Content", emptyImages, 1640995200L, "sender"
        );

        // When & Then
        assertNotNull(dto.getImages());
        assertTrue(dto.getImages().isEmpty());
    }

    @Test
    void testWithMultipleImages() {
        // Given
        List<String> multipleImages = Arrays.asList("img1.jpg", "img2.png", "img3.gif", "img4.webp");
        NotificationResponseDto dto = new NotificationResponseDto(
                1L, "Content with multiple images", multipleImages, 1640995200L, "sender"
        );

        // When & Then
        assertEquals(4, dto.getImages().size());
        assertTrue(dto.getImages().contains("img1.jpg"));
        assertTrue(dto.getImages().contains("img2.png"));
        assertTrue(dto.getImages().contains("img3.gif"));
        assertTrue(dto.getImages().contains("img4.webp"));
    }

    @Test
    void testToString() {
        // Given
        NotificationResponseDto dto = new NotificationResponseDto(
                42L,
                "Test notification",
                Arrays.asList("test1.jpg", "test2.png"),
                1640995200L,
                "test-sender"
        );

        // When
        String result = dto.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("id=42"));
        assertTrue(result.contains("content='Test notification'"));
        assertTrue(result.contains("test1.jpg"));
        assertTrue(result.contains("test2.png"));
        assertTrue(result.contains("sendOn=1640995200"));
        assertTrue(result.contains("from='test-sender'"));
        assertTrue(result.startsWith("NotificationResponseDto{"));
    }

    @Test
    void testToStringWithNullValues() {
        // Given
        NotificationResponseDto dto = new NotificationResponseDto();

        // When
        String result = dto.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("id=null"));
        assertTrue(result.contains("content='null'"));
        assertTrue(result.contains("images=null"));
        assertTrue(result.contains("sendOn=null"));
        assertTrue(result.contains("from='null'"));
    }

    @Test
    void testEqualsAndHashCodeConsistency() {
        // Given
        NotificationResponseDto dto1 = new NotificationResponseDto(
                1L, "content", Arrays.asList("img.jpg"), 1640995200L, "sender"
        );
        NotificationResponseDto dto2 = new NotificationResponseDto(
                1L, "content", Arrays.asList("img.jpg"), 1640995200L, "sender"
        );

        // When & Then - Note: We're not overriding equals/hashCode, so this tests object identity
        assertNotEquals(dto1, dto2); // Different object instances
        assertNotEquals(dto1.hashCode(), dto2.hashCode()); // Different hash codes
    }

    @Test
    void testFieldModification() {
        // Given
        NotificationResponseDto dto = new NotificationResponseDto(
                1L, "original", Arrays.asList("orig.jpg"), 1640995200L, "original-sender"
        );

        // When
        dto.setContent("modified content");
        dto.setFrom("modified-sender");
        dto.setImages(Arrays.asList("new1.jpg", "new2.png"));

        // Then
        assertEquals("modified content", dto.getContent());
        assertEquals("modified-sender", dto.getFrom());
        assertEquals(2, dto.getImages().size());
        assertTrue(dto.getImages().contains("new1.jpg"));
        assertTrue(dto.getImages().contains("new2.png"));
        // Original values should remain for unchanged fields
        assertEquals(1L, dto.getId());
        assertEquals(1640995200L, dto.getSendOn());
    }

    @Test
    void testLongContentHandling() {
        // Given
        String longContent = "a".repeat(5000); // 5000 character content
        NotificationResponseDto dto = new NotificationResponseDto(
                1L, longContent, Collections.emptyList(), 1640995200L, "sender"
        );

        // When & Then
        assertEquals(longContent, dto.getContent());
        assertEquals(5000, dto.getContent().length());
    }

    @Test
    void testTimestampHandling() {
        // Given - Test with various timestamp values
        Long currentTime = System.currentTimeMillis();
        Long pastTime = 1640995200L; // Jan 1, 2022
        Long futureTime = 2000000000L; // May 18, 2033

        // When
        NotificationResponseDto currentDto = new NotificationResponseDto(1L, "current", null, currentTime, "sender");
        NotificationResponseDto pastDto = new NotificationResponseDto(2L, "past", null, pastTime, "sender");
        NotificationResponseDto futureDto = new NotificationResponseDto(3L, "future", null, futureTime, "sender");

        // Then
        assertEquals(currentTime, currentDto.getSendOn());
        assertEquals(pastTime, pastDto.getSendOn());
        assertEquals(futureTime, futureDto.getSendOn());
    }
}