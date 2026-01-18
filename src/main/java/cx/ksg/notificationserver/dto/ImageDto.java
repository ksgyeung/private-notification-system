package cx.ksg.notificationserver.dto;

import cx.ksg.notificationserver.entity.Image;

public class ImageDto {
    private int id;
    private String uuid;
    private transient String filename;
    private String contentType;
    private long size;

    // Constructors
    public ImageDto() {
    }

    public ImageDto(int id, String uuid, String filename, String contentType, long size) {
        this.id = id;
        this.uuid = uuid;
        this.filename = filename;
        this.contentType = contentType;
        this.size = size;
    }

    // Static factory method
    public static ImageDto fromImage(Image image) {
        if (image == null) {
            return null;
        }
        return new ImageDto(
            image.getId(),
            image.getUuid(),
            image.getPath(),
            image.getContentType(),
            image.getSize()
        );
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageDto imageDto = (ImageDto) o;
        return id == imageDto.id &&
               size == imageDto.size &&
               java.util.Objects.equals(uuid, imageDto.uuid) &&
               java.util.Objects.equals(filename, imageDto.filename) &&
               java.util.Objects.equals(contentType, imageDto.contentType);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, uuid, filename, contentType, size);
    }

    // toString
    @Override
    public String toString() {
        return "ImageDto{" +
                "id=" + id +
                ", uuid='" + uuid + '\'' +
                ", filename='" + filename + '\'' +
                ", contentType='" + contentType + '\'' +
                ", size=" + size +
                '}';
    }
}
