package cx.ksg.notificationserver.repository;

import cx.ksg.notificationserver.entity.Notification;
import cx.ksg.notificationserver.entity.NotificationImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for NotificationImageRepository.
 * 
 * Tests the custom query methods and CRUD operations for notification images,
 * ensuring proper functionality for finding and deleting images by notification ID.
 * 
 * Uses @DataJpaTest for focused repository testing with in-memory database.
 */
@DataJpaTest
@ActiveProfiles("test")
class NotificationImageRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NotificationImageRepository notificationImageRepository;

    private Notification testNotification1;
    private Notification testNotification2;
    private NotificationImage testImage1;
    private NotificationImage testImage2;
    private NotificationImage testImage3;

    @BeforeEach
    void setUp() {
        // Create test notifications
        testNotification1 = new Notification();
        testNotification1.setContent("Test notification 1");
        testNotification1.setSendOn(System.currentTimeMillis());
        testNotification1.setFrom("test-sender-1");
        testNotification1.setCreatedAt(LocalDateTime.now());
        testNotification1 = entityManager.persistAndFlush(testNotification1);

        testNotification2 = new Notification();
        testNotification2.setContent("Test notification 2");
        testNotification2.setSendOn(System.currentTimeMillis() + 1000);
        testNotification2.setFrom("test-sender-2");
        testNotification2.setCreatedAt(LocalDateTime.now());
        testNotification2 = entityManager.persistAndFlush(testNotification2);

        // Create test images
        testImage1 = new NotificationImage("/images/test1.jpg", 1024L, testNotification1);
        testImage2 = new NotificationImage("/images/test2.jpg", 2048L, testNotification1);
        testImage3 = new NotificationImage("/images/test3.jpg", 3072L, testNotification2);

        entityManager.persistAndFlush(testImage1);
        entityManager.persistAndFlush(testImage2);
        entityManager.persistAndFlush(testImage3);
    }

    @Test
    void findByNotificationId_ShouldReturnImagesForSpecificNotification() {
        // When
        List<NotificationImage> images = notificationImageRepository.findByNotificationId(testNotification1.getId());

        // Then
        assertThat(images).hasSize(2);
        assertThat(images).extracting(NotificationImage::getFilepath)
                .containsExactlyInAnyOrder("/images/test1.jpg", "/images/test2.jpg");
        assertThat(images).allMatch(image -> image.getNotification().getId().equals(testNotification1.getId()));
    }

    @Test
    void findByNotificationId_ShouldReturnEmptyListForNonExistentNotification() {
        // When
        List<NotificationImage> images = notificationImageRepository.findByNotificationId(999L);

        // Then
        assertThat(images).isEmpty();
    }

    @Test
    void findByNotificationIdOrderById_ShouldReturnImagesInCreationOrder() {
        // When
        List<NotificationImage> images = notificationImageRepository.findByNotificationIdOrderById(testNotification1.getId());

        // Then
        assertThat(images).hasSize(2);
        // Verify they are ordered by ID (creation order)
        assertThat(images.get(0).getId()).isLessThan(images.get(1).getId());
    }

    @Test
    void countByNotificationId_ShouldReturnCorrectCount() {
        // When
        long count1 = notificationImageRepository.countByNotificationId(testNotification1.getId());
        long count2 = notificationImageRepository.countByNotificationId(testNotification2.getId());
        long count3 = notificationImageRepository.countByNotificationId(999L);

        // Then
        assertThat(count1).isEqualTo(2);
        assertThat(count2).isEqualTo(1);
        assertThat(count3).isEqualTo(0);
    }

    @Test
    void existsByNotificationId_ShouldReturnTrueWhenImagesExist() {
        // When
        boolean exists1 = notificationImageRepository.existsByNotificationId(testNotification1.getId());
        boolean exists2 = notificationImageRepository.existsByNotificationId(testNotification2.getId());
        boolean exists3 = notificationImageRepository.existsByNotificationId(999L);

        // Then
        assertThat(exists1).isTrue();
        assertThat(exists2).isTrue();
        assertThat(exists3).isFalse();
    }

    @Test
    void deleteByNotificationId_ShouldDeleteAllImagesForNotification() {
        // Given
        long initialCount = notificationImageRepository.count();
        assertThat(notificationImageRepository.findByNotificationId(testNotification1.getId())).hasSize(2);

        // When
        notificationImageRepository.deleteByNotificationId(testNotification1.getId());
        entityManager.flush();

        // Then
        assertThat(notificationImageRepository.findByNotificationId(testNotification1.getId())).isEmpty();
        assertThat(notificationImageRepository.findByNotificationId(testNotification2.getId())).hasSize(1);
        assertThat(notificationImageRepository.count()).isEqualTo(initialCount - 2);
    }

    @Test
    void deleteByNotificationId_ShouldNotAffectOtherNotifications() {
        // Given
        List<NotificationImage> notification2Images = notificationImageRepository.findByNotificationId(testNotification2.getId());
        assertThat(notification2Images).hasSize(1);

        // When
        notificationImageRepository.deleteByNotificationId(testNotification1.getId());
        entityManager.flush();

        // Then
        List<NotificationImage> remainingImages = notificationImageRepository.findByNotificationId(testNotification2.getId());
        assertThat(remainingImages).hasSize(1);
        assertThat(remainingImages.get(0).getFilepath()).isEqualTo("/images/test3.jpg");
    }

    @Test
    void deleteByNotificationId_ShouldHandleNonExistentNotification() {
        // Given
        long initialCount = notificationImageRepository.count();

        // When
        notificationImageRepository.deleteByNotificationId(999L);
        entityManager.flush();

        // Then
        assertThat(notificationImageRepository.count()).isEqualTo(initialCount);
    }
}