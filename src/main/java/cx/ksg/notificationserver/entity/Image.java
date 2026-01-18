package cx.ksg.notificationserver.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity(name = "image")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String uuid;

    @Column(nullable = false)
    private String path;

    @Column
    private long size;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Image image = (Image) o;
        return id == image.id && 
               size == image.size && 
               java.util.Objects.equals(uuid, image.uuid) &&
               java.util.Objects.equals(path, image.path) && 
               java.util.Objects.equals(contentType, image.contentType);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, uuid, path, size, contentType);
    }

    // toString
    @Override
    public String toString() {
        return "Image{" +
                "id=" + id +
                ", uuid='" + uuid + '\'' +
                ", path='" + path + '\'' +
                ", size=" + size +
                ", contentType='" + contentType + '\'' +
                ", notificationId=" + (notification != null ? notification.getId() : null) +
                '}';
    }
}
