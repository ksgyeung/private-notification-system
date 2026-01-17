package cx.ksg.notificationserver.entity;

import jakarta.persistence.*;

/**
 * JPA Entity representing an image associated with a notification.
 * 
 * This entity maps to the 'notification_images' table and contains:
 * - Unique identifier (id)
 * - File path where the image is stored (filepath)
 * - Size of the image file in bytes (fileSize)
 * - Reference to the parent notification (notification)
 * 
 * The entity maintains a Many-to-One relationship with the Notification entity,
 * allowing multiple images to be associated with a single notification.
 * 
 * Requirements: 4.1, 5.1
 */
@Entity
@Table(name = "notification_images")
public class NotificationImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "filepath", nullable = false)
    private String filepath;

    @Column(name = "file_size")
    private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    // Default constructor
    public NotificationImage() {
    }

    // Constructor with filepath and fileSize
    public NotificationImage(String filepath, Long fileSize) {
        this.filepath = filepath;
        this.fileSize = fileSize;
    }

    // Constructor with filepath, fileSize, and notification
    public NotificationImage(String filepath, Long fileSize, Notification notification) {
        this.filepath = filepath;
        this.fileSize = fileSize;
        this.notification = notification;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        NotificationImage that = (NotificationImage) o;
        
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (filepath != null ? !filepath.equals(that.filepath) : that.filepath != null) return false;
        if (fileSize != null ? !fileSize.equals(that.fileSize) : that.fileSize != null) return false;
        
        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (filepath != null ? filepath.hashCode() : 0);
        result = 31 * result + (fileSize != null ? fileSize.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "NotificationImage{" +
                "id=" + id +
                ", filepath='" + filepath + '\'' +
                ", fileSize=" + fileSize +
                ", notificationId=" + (notification != null ? notification.getId() : null) +
                '}';
    }
}