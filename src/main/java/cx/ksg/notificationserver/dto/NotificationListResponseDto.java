package cx.ksg.notificationserver.dto;

import java.util.List;

/**
 * Data Transfer Object for notification list responses.
 * Contains a list of notifications with pagination metadata.
 */
public class NotificationListResponseDto {

    private List<NotificationResponseDto> notifications;
    private Integer count;
    private Integer totalCount;
    private Integer offset;
    private Integer limit;

    // Default constructor
    public NotificationListResponseDto() {
    }

    // Constructor with all fields
    public NotificationListResponseDto(List<NotificationResponseDto> notifications, Integer count, 
                                     Integer totalCount, Integer offset, Integer limit) {
        this.notifications = notifications;
        this.count = count;
        this.totalCount = totalCount;
        this.offset = offset;
        this.limit = limit;
    }

    // Getters and Setters
    public List<NotificationResponseDto> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<NotificationResponseDto> notifications) {
        this.notifications = notifications;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @Override
    public String toString() {
        return "NotificationListResponseDto{" +
                "notifications=" + notifications +
                ", count=" + count +
                ", totalCount=" + totalCount +
                ", offset=" + offset +
                ", limit=" + limit +
                '}';
    }
}