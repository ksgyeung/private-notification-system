package cx.ksg.notificationserver.repository;

import cx.ksg.notificationserver.entity.Image;
import cx.ksg.notificationserver.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Image entity operations.
 * 
 * Provides CRUD operations and custom query methods for images,
 * with support for finding images by various attributes including UUID and notification relationships.
 * 
 * Key features:
 * - Extends JpaRepository for basic CRUD operations
 * - Custom methods for finding by path, UUID, and content type
 * - Size-based filtering capabilities
 * - Notification relationship queries
 */
@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {

    /**
     * Find an image by its UUID.
     * 
     * @param uuid The UUID of the image
     * @return Optional containing the image if found
     */
    Optional<Image> findByUuid(String uuid);

    /**
     * Find an image by its file path.
     * 
     * @param path The file path of the image
     * @return Optional containing the image if found
     */
    Optional<Image> findByPath(String path);

    /**
     * Find all images by content type.
     * 
     * @param contentType The MIME type of the images
     * @return List of images with the specified content type
     */
    List<Image> findByContentType(String contentType);

    /**
     * Find all images for a specific notification.
     * 
     * @param notification The notification entity
     * @return List of images associated with the notification
     */
    List<Image> findByNotification(Notification notification);

    /**
     * Find all images by notification ID.
     * 
     * @param notificationId The ID of the notification
     * @return List of images associated with the notification
     */
    List<Image> findByNotificationId(Long notificationId);

    /**
     * Find all images larger than the specified size.
     * 
     * @param size The minimum size in bytes
     * @return List of images larger than the specified size
     */
    List<Image> findBySizeGreaterThan(long size);

    /**
     * Find all images smaller than the specified size.
     * 
     * @param size The maximum size in bytes
     * @return List of images smaller than the specified size
     */
    List<Image> findBySizeLessThan(long size);

    /**
     * Find all images within a size range.
     * 
     * @param minSize The minimum size in bytes (inclusive)
     * @param maxSize The maximum size in bytes (inclusive)
     * @return List of images within the specified size range
     */
    List<Image> findBySizeBetween(long minSize, long maxSize);

    /**
     * Check if an image exists with the given UUID.
     * 
     * @param uuid The UUID to check
     * @return true if an image exists with the given UUID
     */
    boolean existsByUuid(String uuid);

    /**
     * Check if an image exists with the given path.
     * 
     * @param path The file path to check
     * @return true if an image exists with the given path
     */
    boolean existsByPath(String path);

    /**
     * Count images for a specific notification.
     * 
     * @param notificationId The ID of the notification
     * @return Number of images associated with the notification
     */
    long countByNotificationId(Long notificationId);
}