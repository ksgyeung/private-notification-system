package cx.ksg.notificationserver.repository;

import cx.ksg.notificationserver.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Notification entity operations.
 * 
 * Provides CRUD operations and custom query methods for notifications,
 * with support for ordering by sendOn timestamp and pagination.
 * 
 * Key features:
 * - Extends JpaRepository for basic CRUD operations
 * - Custom methods for ordering by sendOn descending (newest first)
 * - Pagination support for large result sets
 * - Timestamp range filtering capabilities
 * 
 * Requirements: 5.4, 3.2
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find all notifications ordered by sendOn timestamp in descending order (newest first).
     * 
     * @return List of notifications ordered by sendOn descending
     */
    List<Notification> findAllByOrderBySendOnDesc();

    /**
     * Find all notifications with pagination, ordered by sendOn timestamp in descending order.
     * 
     * @param pageable Pagination parameters (page number, size, etc.)
     * @return Page of notifications ordered by sendOn descending
     */
    Page<Notification> findAllByOrderBySendOnDesc(Pageable pageable);

    /**
     * Find notifications within a specific timestamp range, ordered by sendOn descending.
     * 
     * @param fromTimestamp Start timestamp (inclusive)
     * @param toTimestamp End timestamp (inclusive)
     * @return List of notifications within the timestamp range
     */
    @Query("SELECT n FROM Notification n WHERE n.sendOn >= :fromTimestamp AND n.sendOn <= :toTimestamp ORDER BY n.sendOn DESC")
    List<Notification> findByTimestampRange(@Param("fromTimestamp") Long fromTimestamp, 
                                          @Param("toTimestamp") Long toTimestamp);

    /**
     * Find notifications within a specific timestamp range with pagination, ordered by sendOn descending.
     * 
     * @param fromTimestamp Start timestamp (inclusive)
     * @param toTimestamp End timestamp (inclusive)
     * @param pageable Pagination parameters
     * @return Page of notifications within the timestamp range
     */
    @Query("SELECT n FROM Notification n WHERE n.sendOn >= :fromTimestamp AND n.sendOn <= :toTimestamp ORDER BY n.sendOn DESC")
    Page<Notification> findByTimestampRange(@Param("fromTimestamp") Long fromTimestamp, 
                                          @Param("toTimestamp") Long toTimestamp, 
                                          Pageable pageable);

    /**
     * Find notifications from a specific timestamp onwards, ordered by sendOn descending.
     * 
     * @param fromTimestamp Start timestamp (inclusive)
     * @return List of notifications from the specified timestamp
     */
    List<Notification> findBySendOnGreaterThanEqualOrderBySendOnDesc(Long fromTimestamp);

    /**
     * Find notifications from a specific timestamp onwards with pagination, ordered by sendOn descending.
     * 
     * @param fromTimestamp Start timestamp (inclusive)
     * @param pageable Pagination parameters
     * @return Page of notifications from the specified timestamp
     */
    Page<Notification> findBySendOnGreaterThanEqualOrderBySendOnDesc(Long fromTimestamp, Pageable pageable);

    /**
     * Find notifications up to a specific timestamp, ordered by sendOn descending.
     * 
     * @param toTimestamp End timestamp (inclusive)
     * @return List of notifications up to the specified timestamp
     */
    List<Notification> findBySendOnLessThanEqualOrderBySendOnDesc(Long toTimestamp);

    /**
     * Find notifications up to a specific timestamp with pagination, ordered by sendOn descending.
     * 
     * @param toTimestamp End timestamp (inclusive)
     * @param pageable Pagination parameters
     * @return Page of notifications up to the specified timestamp
     */
    Page<Notification> findBySendOnLessThanEqualOrderBySendOnDesc(Long toTimestamp, Pageable pageable);
}