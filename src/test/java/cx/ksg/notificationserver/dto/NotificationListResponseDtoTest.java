package cx.ksg.notificationserver.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NotificationListResponseDto functionality.
 */
class NotificationListResponseDtoTest {

    @Test
    void testDefaultConstructor() {
        // When
        NotificationListResponseDto dto = new NotificationListResponseDto();

        // Then
        assertNull(dto.getNotifications());
        assertNull(dto.getTotalCount());
        assertNull(dto.getCount());
        assertNull(dto.getLimit());
        assertNull(dto.getOffset());
    }

    @Test
    void testConstructorWithNotificationsOnly() {
        // Given
        List<NotificationResponseDto> notifications = Arrays.asList(
                new NotificationResponseDto(1L, "content1", Collections.emptyList(), 1640995200L, "sender1"),
                new NotificationResponseDto(2L, "content2", Collections.emptyList(), 1641081600L, "sender2")
        );

        // When
        NotificationListResponseDto dto = new NotificationListResponseDto(notifications);

        // Then
        assertEquals(notifications, dto.getNotifications());
        assertEquals(2, dto.getCount());
        assertNull(dto.getTotalCount());
        assertNull(dto.getLimit());
        assertNull(dto.getOffset());
    }

    @Test
    void testConstructorWithEmptyNotificationsList() {
        // Given
        List<NotificationResponseDto> emptyNotifications = Collections.emptyList();

        // When
        NotificationListResponseDto dto = new NotificationListResponseDto(emptyNotifications);

        // Then
        assertEquals(emptyNotifications, dto.getNotifications());
        assertEquals(0, dto.getCount());
        assertNull(dto.getTotalCount());
        assertNull(dto.getLimit());
        assertNull(dto.getOffset());
    }

    @Test
    void testConstructorWithNullNotificationsList() {
        // When
        NotificationListResponseDto dto = new NotificationListResponseDto(null);

        // Then
        assertNull(dto.getNotifications());
        assertEquals(0, dto.getCount());
        assertNull(dto.getTotalCount());
        assertNull(dto.getLimit());
        assertNull(dto.getOffset());
    }

    @Test
    void testConstructorWithAllFields() {
        // Given
        List<NotificationResponseDto> notifications = Arrays.asList(
                new NotificationResponseDto(1L, "content1", Collections.emptyList(), 1640995200L, "sender1"),
                new NotificationResponseDto(2L, "content2", Collections.emptyList(), 1641081600L, "sender2")
        );
        Long totalCount = 100L;
        Integer limit = 10;
        Integer offset = 20;

        // When
        NotificationListResponseDto dto = new NotificationListResponseDto(notifications, totalCount, limit, offset);

        // Then
        assertEquals(notifications, dto.getNotifications());
        assertEquals(2, dto.getCount()); // Actual count of notifications in the list
        assertEquals(totalCount, dto.getTotalCount());
        assertEquals(limit, dto.getLimit());
        assertEquals(offset, dto.getOffset());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        NotificationListResponseDto dto = new NotificationListResponseDto();
        List<NotificationResponseDto> notifications = Arrays.asList(
                new NotificationResponseDto(1L, "test", Collections.emptyList(), 1640995200L, "sender")
        );
        Long totalCount = 50L;
        Integer limit = 5;
        Integer offset = 10;

        // When
        dto.setNotifications(notifications);
        dto.setTotalCount(totalCount);
        dto.setLimit(limit);
        dto.setOffset(offset);

        // Then
        assertEquals(notifications, dto.getNotifications());
        assertEquals(1, dto.getCount()); // Auto-calculated from notifications list
        assertEquals(totalCount, dto.getTotalCount());
        assertEquals(limit, dto.getLimit());
        assertEquals(offset, dto.getOffset());
    }

    @Test
    void testSetNotificationsUpdatesCount() {
        // Given
        NotificationListResponseDto dto = new NotificationListResponseDto();
        List<NotificationResponseDto> initialNotifications = Arrays.asList(
                new NotificationResponseDto(1L, "content1", Collections.emptyList(), 1640995200L, "sender1")
        );
        List<NotificationResponseDto> updatedNotifications = Arrays.asList(
                new NotificationResponseDto(1L, "content1", Collections.emptyList(), 1640995200L, "sender1"),
                new NotificationResponseDto(2L, "content2", Collections.emptyList(), 1641081600L, "sender2"),
                new NotificationResponseDto(3L, "content3", Collections.emptyList(), 1641168000L, "sender3")
        );

        // When
        dto.setNotifications(initialNotifications);
        assertEquals(1, dto.getCount());

        dto.setNotifications(updatedNotifications);

        // Then
        assertEquals(3, dto.getCount());
        assertEquals(updatedNotifications, dto.getNotifications());
    }

    @Test
    void testSetNotificationsWithNull() {
        // Given
        NotificationListResponseDto dto = new NotificationListResponseDto();

        // When
        dto.setNotifications(null);

        // Then
        assertNull(dto.getNotifications());
        assertEquals(0, dto.getCount());
    }

    @Test
    void testSetCountDirectly() {
        // Given
        NotificationListResponseDto dto = new NotificationListResponseDto();

        // When
        dto.setCount(42);

        // Then
        assertEquals(42, dto.getCount());
    }

    @Test
    void testToString() {
        // Given
        List<NotificationResponseDto> notifications = Arrays.asList(
                new NotificationResponseDto(1L, "content1", Collections.emptyList(), 1640995200L, "sender1")
        );
        NotificationListResponseDto dto = new NotificationListResponseDto(notifications, 100L, 10, 20);

        // When
        String result = dto.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("notifications="));
        assertTrue(result.contains("totalCount=100"));
        assertTrue(result.contains("count=1"));
        assertTrue(result.contains("limit=10"));
        assertTrue(result.contains("offset=20"));
        assertTrue(result.startsWith("NotificationListResponseDto{"));
    }

    @Test
    void testToStringWithNullValues() {
        // Given
        NotificationListResponseDto dto = new NotificationListResponseDto();

        // When
        String result = dto.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("notifications=null"));
        assertTrue(result.contains("totalCount=null"));
        assertTrue(result.contains("count=null"));
        assertTrue(result.contains("limit=null"));
        assertTrue(result.contains("offset=null"));
    }

    @Test
    void testPaginationScenario() {
        // Given - Simulating a paginated response
        List<NotificationResponseDto> pageNotifications = Arrays.asList(
                new NotificationResponseDto(11L, "content11", Collections.emptyList(), 1640995200L, "sender11"),
                new NotificationResponseDto(12L, "content12", Collections.emptyList(), 1641081600L, "sender12"),
                new NotificationResponseDto(13L, "content13", Collections.emptyList(), 1641168000L, "sender13")
        );
        Long totalCount = 150L; // Total notifications in database
        Integer limit = 3;      // Page size
        Integer offset = 30;    // Starting from 30th record

        // When
        NotificationListResponseDto dto = new NotificationListResponseDto(pageNotifications, totalCount, limit, offset);

        // Then
        assertEquals(3, dto.getCount());           // Actual notifications returned
        assertEquals(150L, dto.getTotalCount());   // Total available
        assertEquals(3, dto.getLimit());           // Requested page size
        assertEquals(30, dto.getOffset());         // Starting position
        assertEquals(pageNotifications, dto.getNotifications());
    }

    @Test
    void testEmptyResultScenario() {
        // Given - No notifications found
        List<NotificationResponseDto> emptyNotifications = Collections.emptyList();
        Long totalCount = 0L;
        Integer limit = 10;
        Integer offset = 0;

        // When
        NotificationListResponseDto dto = new NotificationListResponseDto(emptyNotifications, totalCount, limit, offset);

        // Then
        assertEquals(0, dto.getCount());
        assertEquals(0L, dto.getTotalCount());
        assertEquals(10, dto.getLimit());
        assertEquals(0, dto.getOffset());
        assertTrue(dto.getNotifications().isEmpty());
    }

    @Test
    void testLargeDatasetScenario() {
        // Given - Large number of notifications
        List<NotificationResponseDto> notifications = Arrays.asList(
                new NotificationResponseDto(1L, "content1", Arrays.asList("img1.jpg", "img2.png"), 1640995200L, "sender1"),
                new NotificationResponseDto(2L, "content2", Collections.singletonList("img3.jpg"), 1641081600L, "sender2"),
                new NotificationResponseDto(3L, "content3", Collections.emptyList(), 1641168000L, "sender3")
        );
        Long totalCount = 10000L;
        Integer limit = 3;
        Integer offset = 5000;

        // When
        NotificationListResponseDto dto = new NotificationListResponseDto(notifications, totalCount, limit, offset);

        // Then
        assertEquals(3, dto.getCount());
        assertEquals(10000L, dto.getTotalCount());
        assertEquals(3, dto.getLimit());
        assertEquals(5000, dto.getOffset());
        
        // Verify notifications have their images preserved
        assertEquals(2, dto.getNotifications().get(0).getImages().size());
        assertEquals(1, dto.getNotifications().get(1).getImages().size());
        assertEquals(0, dto.getNotifications().get(2).getImages().size());
    }
}