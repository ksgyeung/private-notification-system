package cx.ksg.notificationserver.repository;

import cx.ksg.notificationserver.entity.NotificationImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository interface for NotificationImage entity operations.
 * 
 * Provides CRUD operations and custom query methods for notification images,
 * with support for finding and deleting images by notification ID.
 * 
 * Key features:
 * - Extends JpaRepository for basic CRUD operations
 * - Custom methods for finding images by notification ID
 * - Bulk delete operations for notification cleanup
 * - Transactional support for data consistency
 * 
 * Requirements: 5.4
 */
@Repository
public interface NotificationImageRepository extends JpaRepository<NotificationImage, Long> {

    /**
     * Find all images associated with a specific notification.
     * 
     * @param notificationId The ID of the notification
     * @return List of NotificationImage entities associated with the notification
     */
    List<NotificationImage> findByNotificationId(Long notificationId);

    /**
     * Delete all images associated with a specific notification.
     * This method is typically used when a notification is deleted to maintain referential integrity.
     * 
     * @param notificationId The ID of the notification whose images should be deleted
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM NotificationImage ni WHERE ni.notification.id = :notificationId")
    void deleteByNotificationId(@Param("notificationId") Long notificationId);

    /**
     * Count the number of images associated with a specific notification.
     * 
     * @param notificationId The ID of the notification
     * @return The count of images associated with the notification
     */
    long countByNotificationId(Long notificationId);

    /**
     * Check if any images exist for a specific notification.
     * 
     * @param notificationId The ID of the notification
     * @return true if images exist for the notification, false otherwise
     */
    boolean existsByNotificationId(Long notificationId);

    /**
     * Find images by notification ID ordered by ID (creation order).
     * 
     * @param notificationId The ID of the notification
     * @return List of NotificationImage entities ordered by ID
     */
    List<NotificationImage> findByNotificationIdOrderById(Long notificationId);
}