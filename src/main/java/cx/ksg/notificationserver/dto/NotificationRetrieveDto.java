package cx.ksg.notificationserver.dto;

import java.util.Objects;

public class NotificationRetrieveDto {
    private long lastId;

    public long getLastId() {
        return lastId;
    }

    public void setLastId(long lastId) {
        this.lastId = lastId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationRetrieveDto that = (NotificationRetrieveDto) o;
        return lastId == that.lastId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lastId);
    }

    @Override
    public String toString() {
        return "NotificationRetrieveDto{" +
                "lastId=" + lastId +
                '}';
    }
}