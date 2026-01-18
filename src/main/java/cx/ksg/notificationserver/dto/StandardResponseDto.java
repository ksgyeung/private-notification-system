package cx.ksg.notificationserver.dto;

public class StandardResponseDto<T> {
    private final boolean success;
    private final String message;
    private final T data;

    public StandardResponseDto(boolean success) {
        this(success, "", null);
    }

    public StandardResponseDto(boolean success, String message) {
        this(success, message, null);
    }
    
    public StandardResponseDto(boolean success, T data) {
        this(success, "", data);
    }

    public StandardResponseDto(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }
}
