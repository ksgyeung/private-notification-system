package cx.ksg.notificationserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Data Transfer Object for creating notifications.
 * Contains all required fields for notification creation.
 */
public class NotificationCreateDto {

    @NotBlank(message = "Content cannot be blank")
    private String content;

    @NotNull(message = "Send on timestamp is required")
    private Long sendOn;

    @NotBlank(message = "From field cannot be blank")
    private String from;

    private List<String> imageUuids;

    // Default constructor
    public NotificationCreateDto() {
    }

    // Constructor with all fields
    public NotificationCreateDto(String content, Long sendOn, String from, List<String> imageUuids) {
        this.content = content;
        this.sendOn = sendOn;
        this.from = from;
        this.imageUuids = imageUuids;
    }

    // Getters and Setters
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

    public List<String> getImageUuids() {
        return imageUuids;
    }

    public void setImageUuids(List<String> imageUuids) {
        this.imageUuids = imageUuids;
    }

    @Override
    public String toString() {
        return "NotificationCreateDto{" +
                "content='" + content + '\'' +
                ", sendOn=" + sendOn +
                ", from='" + from + '\'' +
                ", imageUuids=" + imageUuids +
                '}';
    }
}