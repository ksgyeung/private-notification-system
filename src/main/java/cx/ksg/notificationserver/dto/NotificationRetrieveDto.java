package cx.ksg.notificationserver.dto;

import jakarta.validation.constraints.Min;

/**
 * Data Transfer Object for retrieving notifications.
 * Contains optional filtering and pagination parameters for notification retrieval.
 */
public class NotificationRetrieveDto {

    /**
     * Maximum number of notifications to return.
     * Optional field with minimum value validation.
     */
    @Min(value = 1, message = "Limit must be at least 1")
    private Integer limit;

    /**
     * Number of notifications to skip for pagination.
     * Optional field with minimum value validation.
     */
    @Min(value = 0, message = "Offset must be non-negative")
    private Integer offset;

    /**
     * Unix epoch timestamp to filter notifications from (inclusive).
     * Optional field for timestamp-based filtering.
     */
    private Long fromTimestamp;

    /**
     * Unix epoch timestamp to filter notifications to (inclusive).
     * Optional field for timestamp-based filtering.
     */
    private Long toTimestamp;

    // Default constructor
    public NotificationRetrieveDto() {
    }

    // Constructor with all fields
    public NotificationRetrieveDto(Integer limit, Integer offset, Long fromTimestamp, Long toTimestamp) {
        this.limit = limit;
        this.offset = offset;
        this.fromTimestamp = fromTimestamp;
        this.toTimestamp = toTimestamp;
    }

    // Getters and setters
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

    public Long getFromTimestamp() {
        return fromTimestamp;
    }

    public void setFromTimestamp(Long fromTimestamp) {
        this.fromTimestamp = fromTimestamp;
    }

    public Long getToTimestamp() {
        return toTimestamp;
    }

    public void setToTimestamp(Long toTimestamp) {
        this.toTimestamp = toTimestamp;
    }

    @Override
    public String toString() {
        return "NotificationRetrieveDto{" +
                "limit=" + limit +
                ", offset=" + offset +
                ", fromTimestamp=" + fromTimestamp +
                ", toTimestamp=" + toTimestamp +
                '}';
    }
}