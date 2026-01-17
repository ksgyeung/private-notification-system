package cx.ksg.notificationserver.dto;

import java.util.List;

/**
 * Data Transfer Object for notification list responses.
 * Contains a list of notifications and optional pagination metadata.
 * 
 * Requirements: 3.3, 6.3
 */
public class NotificationListResponseDto {

    /**
     * List of notifications matching the request criteria.
     * Empty list if no notifications match the criteria.
     */
    private List<NotificationResponseDto> notifications;

    /**
     * Total number of notifications available (for pagination).
     * Optional field that can be used for pagination metadata.
     */
    private Long totalCount;

    /**
     * Number of notifications returned in this response.
     * Useful for pagination and result set information.
     */
    private Integer count;

    /**
     * The limit parameter used in the request.
     * Optional field for pagination metadata.
     */
    private Integer limit;

    /**
     * The offset parameter used in the request.
     * Optional field for pagination metadata.
     */
    private Integer offset;

    // Default constructor
    public NotificationListResponseDto() {
    }

    // Constructor with notifications only (minimal response)
    public NotificationListResponseDto(List<NotificationResponseDto> notifications) {
        this.notifications = notifications;
        this.count = notifications != null ? notifications.size() : 0;
    }

    // Constructor with all fields (full pagination response)
    public NotificationListResponseDto(List<NotificationResponseDto> notifications, Long totalCount, 
                                     Integer limit, Integer offset) {
        this.notifications = notifications;
        this.totalCount = totalCount;
        this.count = notifications != null ? notifications.size() : 0;
        this.limit = limit;
        this.offset = offset;
    }

    // Getters and setters
    public List<NotificationResponseDto> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<NotificationResponseDto> notifications) {
        this.notifications = notifications;
        this.count = notifications != null ? notifications.size() : 0;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "NotificationListResponseDto{" +
                "notifications=" + notifications +
                ", totalCount=" + totalCount +
                ", count=" + count +
                ", limit=" + limit +
                ", offset=" + offset +
                '}';
    }
}