package cx.ksg.notificationserver.dto;

import java.util.List;
import java.util.stream.Collectors;

public class NotificationResponseDto2 {

    /**
     * Unique identifier of the notification.
     * Auto-generated primary key from the database.
     */
    private Long id;

    /**
     * The notification content/message.
     * Contains the actual notification text.
     */
    private String content;

    /**
     * List of image filenames associated with the notification.
     * Empty list if no images are attached.
     */
    private List<String> images;

    /**
     * Unix epoch timestamp indicating when the notification should be sent.
     * Used for chronological ordering.
     */
    private Long sendOn;

    /**
     * Sender information identifying who created the notification.
     * Contains the original sender/issuer information.
     */
    private String from;

    // Default constructor
    public NotificationResponseDto2() {
    }

    // Constructor with all fields
    public NotificationResponseDto2(Long id, String content, List<String> images, Long sendOn, String from) {
        this.id = id;
        this.content = content;
        this.images = images;
        this.sendOn = sendOn;
        this.from = from;
    }

    public static NotificationResponseDto2 fromNotificationResponseDto(String hostUrl, NotificationResponseDto responseDto) {
        return new NotificationResponseDto2(
            responseDto.getId(),
            responseDto.getContent(),
            responseDto.getImages().stream().map(x -> hostUrl + x.getFilename()).toList(),
            responseDto.getSendOn(),
            responseDto.getFrom()
        );
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public Long getSendOn() {
        return sendOn;
    }

    public void setSendOn(Long sendOn) {
        this.sendOn = sendOn;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @Override
    public String toString() {
        return "NotificationResponseDto{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", images=" + images +
                ", sendOn=" + sendOn +
                ", from='" + from + '\'' +
                '}';
    }
}