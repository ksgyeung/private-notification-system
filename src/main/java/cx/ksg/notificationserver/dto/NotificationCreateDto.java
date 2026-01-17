package cx.ksg.notificationserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Data Transfer Object for creating notifications.
 * Contains all required fields and validation constraints for notification creation.
 */
public class NotificationCreateDto {

    /**
     * The notification content/message.
     * Required field with maximum length validation.
     */
    @NotBlank(message = "Content is required and cannot be blank")
    @Size(max = 10000, message = "Content cannot exceed 10000 characters")
    private String content;

    /**
     * Unix epoch timestamp indicating when the notification should be sent.
     * Required field.
     */
    @NotNull(message = "Send timestamp (sendOn) is required")
    private Long sendOn;

    /**
     * Sender information identifying who created the notification.
     * Required field.
     */
    @NotBlank(message = "From field is required and cannot be blank")
    private String from;

    /**
     * Optional list of image filenames associated with the notification.
     * Can be null or empty if no images are attached.
     */
    private List<String> images;

    // Default constructor
    public NotificationCreateDto() {
    }

    // Constructor with all fields
    public NotificationCreateDto(String content, Long sendOn, String from, List<String> images) {
        this.content = content;
        this.sendOn = sendOn;
        this.from = from;
        this.images = images;
    }

    // Getters and setters
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    @Override
    public String toString() {
        return "NotificationCreateDto{" +
                "content='" + content + '\'' +
                ", sendOn=" + sendOn +
                ", from='" + from + '\'' +
                ", images=" + images +
                '}';
    }
}