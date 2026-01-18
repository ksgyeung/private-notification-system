package cx.ksg.notificationserver.exception;

import org.springframework.web.multipart.MultipartFile;

public class InvalidImageException extends Exception {
    private final MultipartFile multipartFile;

    public InvalidImageException(String message) {
        this(null, message);
    }

    public InvalidImageException(MultipartFile multipartFile, String message) {
        super(message);
        this.multipartFile = multipartFile;
    }

    public MultipartFile getMultipartFile() {
        return multipartFile;
    }
}
