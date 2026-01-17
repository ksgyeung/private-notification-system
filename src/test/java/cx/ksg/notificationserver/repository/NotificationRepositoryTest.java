package cx.ksg.notificationserver.repository;

import cx.ksg.notificationserver.entity.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NotificationRepository.
 * Tests repository methods for ordering, pagination, and timestamp filtering.
 * 
 * Requirements: 5.4, 3.2
 */
@DataJpaTest
@ActiveProfiles("test")
class NotificationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NotificationRepository notificationRepository;

    private Notification notification1;
    private Notification notification2;
    private Notification notification3;

    @BeforeEach
    void setUp() {
        // Create test notifications with different timestamps
        // notification1: oldest (1640995200L = 2022-01-01 00:00:00 UTC)
        notification1 = new Notification("First notification", 1640995200L, "sender1");
        
        // notification2: middle (1641081600L = 2022-01-02 00:00:00 UTC)
        notification2 = new Notification("Second notification", 1641081600L, "sender2");
        
        // notification3: newest (1641168000L = 2022-01-03 00:00:00 UTC)
        notification3 = new Notification("Third notification", 1641168000L, "sender3");

        // Persist test data
        entityManager.persistAndFlush(notification1);
        entityManager.persistAndFlush(notification2);
        entityManager.persistAndFlush(notification3);
    }

    @Test
    void testFindAllByOrderBySendOnDesc() {
        // When
        List<Notification> notifications = notificationRepository.findAllByOrderBySendOnDesc();

        // Then
        assertNotNull(notifications);
        assertEquals(3, notifications.size());
        
        // Verify descending order (newest first)
        assertEquals(notification3.getSendOn(), notifications.get(0).getSendOn());
        assertEquals(notification2.getSendOn(), notifications.get(1).getSendOn());
        assertEquals(notification1.getSendOn(), notifications.get(2).getSendOn());
    }

    @Test
    void testFindAllByOrderBySendOnDescWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 2); // First page, 2 items

        // When
        Page<Notification> page = notificationRepository.findAllByOrderBySendOnDesc(pageable);

        // Then
        assertNotNull(page);
        assertEquals(2, page.getContent().size());
        assertEquals(3, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
        assertTrue(page.isFirst());
        assertFalse(page.isLast());
        
        // Verify descending order (newest first)
        List<Notification> content = page.getContent();
        assertEquals(notification3.getSendOn(), content.get(0).getSendOn());
        assertEquals(notification2.getSendOn(), content.get(1).getSendOn());
    }

    @Test
    void testFindAllByOrderBySendOnDescWithPaginationSecondPage() {
        // Given
        Pageable pageable = PageRequest.of(1, 2); // Second page, 2 items

        // When
        Page<Notification> page = notificationRepository.findAllByOrderBySendOnDesc(pageable);

        // Then
        assertNotNull(page);
        assertEquals(1, page.getContent().size());
        assertEquals(3, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
        assertFalse(page.isFirst());
        assertTrue(page.isLast());
        
        // Verify it contains the oldest notification
        List<Notification> content = page.getContent();
        assertEquals(notification1.getSendOn(), content.get(0).getSendOn());
    }

    @Test
    void testFindByTimestampRange() {
        // Given - range that includes notification2 and notification3
        Long fromTimestamp = 1641081600L; // 2022-01-02 00:00:00 UTC
        Long toTimestamp = 1641168000L;   // 2022-01-03 00:00:00 UTC

        // When
        List<Notification> notifications = notificationRepository.findByTimestampRange(fromTimestamp, toTimestamp);

        // Then
        assertNotNull(notifications);
        assertEquals(2, notifications.size());
        
        // Verify descending order and correct notifications
        assertEquals(notification3.getSendOn(), notifications.get(0).getSendOn());
        assertEquals(notification2.getSendOn(), notifications.get(1).getSendOn());
    }

    @Test
    void testFindByTimestampRangeWithPagination() {
        // Given - range that includes all notifications
        Long fromTimestamp = 1640995200L; // 2022-01-01 00:00:00 UTC
        Long toTimestamp = 1641168000L;   // 2022-01-03 00:00:00 UTC
        Pageable pageable = PageRequest.of(0, 2);

        // When
        Page<Notification> page = notificationRepository.findByTimestampRange(fromTimestamp, toTimestamp, pageable);

        // Then
        assertNotNull(page);
        assertEquals(2, page.getContent().size());
        assertEquals(3, page.getTotalElements());
        
        // Verify descending order
        List<Notification> content = page.getContent();
        assertEquals(notification3.getSendOn(), content.get(0).getSendOn());
        assertEquals(notification2.getSendOn(), content.get(1).getSendOn());
    }

    @Test
    void testFindByTimestampRangeNoResults() {
        // Given - range with no matching notifications
        Long fromTimestamp = 1640908800L; // 2021-12-31 00:00:00 UTC
        Long toTimestamp = 1640908800L;   // 2021-12-31 00:00:00 UTC

        // When
        List<Notification> notifications = notificationRepository.findByTimestampRange(fromTimestamp, toTimestamp);

        // Then
        assertNotNull(notifications);
        assertTrue(notifications.isEmpty());
    }

    @Test
    void testFindBySendOnGreaterThanEqualOrderBySendOnDesc() {
        // Given - timestamp that should include notification2 and notification3
        Long fromTimestamp = 1641081600L; // 2022-01-02 00:00:00 UTC

        // When
        List<Notification> notifications = notificationRepository.findBySendOnGreaterThanEqualOrderBySendOnDesc(fromTimestamp);

        // Then
        assertNotNull(notifications);
        assertEquals(2, notifications.size());
        
        // Verify descending order
        assertEquals(notification3.getSendOn(), notifications.get(0).getSendOn());
        assertEquals(notification2.getSendOn(), notifications.get(1).getSendOn());
    }

    @Test
    void testFindBySendOnGreaterThanEqualOrderBySendOnDescWithPagination() {
        // Given
        Long fromTimestamp = 1640995200L; // 2022-01-01 00:00:00 UTC (includes all)
        Pageable pageable = PageRequest.of(0, 2);

        // When
        Page<Notification> page = notificationRepository.findBySendOnGreaterThanEqualOrderBySendOnDesc(fromTimestamp, pageable);

        // Then
        assertNotNull(page);
        assertEquals(2, page.getContent().size());
        assertEquals(3, page.getTotalElements());
        
        // Verify descending order
        List<Notification> content = page.getContent();
        assertEquals(notification3.getSendOn(), content.get(0).getSendOn());
        assertEquals(notification2.getSendOn(), content.get(1).getSendOn());
    }

    @Test
    void testFindBySendOnLessThanEqualOrderBySendOnDesc() {
        // Given - timestamp that should include notification1 and notification2
        Long toTimestamp = 1641081600L; // 2022-01-02 00:00:00 UTC

        // When
        List<Notification> notifications = notificationRepository.findBySendOnLessThanEqualOrderBySendOnDesc(toTimestamp);

        // Then
        assertNotNull(notifications);
        assertEquals(2, notifications.size());
        
        // Verify descending order
        assertEquals(notification2.getSendOn(), notifications.get(0).getSendOn());
        assertEquals(notification1.getSendOn(), notifications.get(1).getSendOn());
    }

    @Test
    void testFindBySendOnLessThanEqualOrderBySendOnDescWithPagination() {
        // Given
        Long toTimestamp = 1641168000L; // 2022-01-03 00:00:00 UTC (includes all)
        Pageable pageable = PageRequest.of(0, 2);

        // When
        Page<Notification> page = notificationRepository.findBySendOnLessThanEqualOrderBySendOnDesc(toTimestamp, pageable);

        // Then
        assertNotNull(page);
        assertEquals(2, page.getContent().size());
        assertEquals(3, page.getTotalElements());
        
        // Verify descending order
        List<Notification> content = page.getContent();
        assertEquals(notification3.getSendOn(), content.get(0).getSendOn());
        assertEquals(notification2.getSendOn(), content.get(1).getSendOn());
    }

    @Test
    void testEmptyRepository() {
        // Given - clear all data
        notificationRepository.deleteAll();
        entityManager.flush();

        // When
        List<Notification> notifications = notificationRepository.findAllByOrderBySendOnDesc();

        // Then
        assertNotNull(notifications);
        assertTrue(notifications.isEmpty());
    }

    @Test
    void testPaginationBeyondAvailableData() {
        // Given - request page beyond available data
        Pageable pageable = PageRequest.of(10, 5); // Page 10, 5 items per page

        // When
        Page<Notification> page = notificationRepository.findAllByOrderBySendOnDesc(pageable);

        // Then
        assertNotNull(page);
        assertTrue(page.getContent().isEmpty());
        assertEquals(3, page.getTotalElements());
        assertEquals(1, page.getTotalPages()); // Only 1 page needed for 3 items with 5 per page
    }
}