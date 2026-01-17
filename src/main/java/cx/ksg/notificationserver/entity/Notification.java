package cx.ksg.notificationserver.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA Entity representing a notification in the system.
 * 
 * This entity maps to the 'notifications' table and contains:
 * - Unique identifier (id)
 * - Notification content (content)
 * - Unix epoch timestamp for when to send (sendOn)
 * - Sender information (from)
 * - Creation timestamp (createdAt)
 * - Associated images (images)
 * 
 * Requirements: 4.1, 5.1
 */
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "send_on")
    private Long sendOn;

    @Column(name = "from_sender")
    private String from;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NotificationImage> images;

    // Default constructor
    public Notification() {
    }

    // Constructor with required fields
    public Notification(String content, Long sendOn, String from) {
        this.content = content;
        this.sendOn = sendOn;
        this.from = from;
    }

    // Getters and Setters
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<NotificationImage> getImages() {
        return images;
    }

    public void setImages(List<NotificationImage> images) {
        this.images = images;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", sendOn=" + sendOn +
                ", from='" + from + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}