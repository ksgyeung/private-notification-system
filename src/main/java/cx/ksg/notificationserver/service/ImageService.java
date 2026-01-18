package cx.ksg.notificationserver.service;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.luciad.imageio.webp.WebPReadParam;

import cx.ksg.notificationserver.exception.InvalidImageException;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service for handling image file operations in the notification system.
 * 
 * This service provides functionality to:
 * - Save uploaded image files to the configured storage directory
 * - Validate image file types and sizes
 * - Generate unique filenames to prevent conflicts
 * - Create storage directories if they don't exist
 * - Retrieve full paths to stored images
 * 
 * The image storage path is configurable via application.yaml property:
 * notification.image-storage-path
 * 
 * Requirements: 2.3, 10.1, 10.2, 10.3
 */
@Service
public class ImageService implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

    // Supported image file extensions
    private static final Set<String> SUPPORTED_IMAGE_EXTENSIONS = Set.of(
        "jpg", "jpeg", "png", "gif", "bmp", "webp"
    );

    // Maximum file size in bytes (10MB as configured in application.yaml)
    @Value("${notification.image.max-size * 1024 * 1024}")
    private long maxFileSize;

    @Value("${notification.image.storage-path}")
    private String imageStoragePath;

    /**
     * Saves multiple image files to the configured storage directory.
     * 
     * This method:
     * - Creates the storage directory if it doesn't exist
     * - Validates each image file
     * - Generates unique filenames to prevent conflicts
     * - Saves files to the storage directory
     * - Returns list of saved filenames for database storage
     * 
     * @param images List of MultipartFile objects representing uploaded images
     * @return List of saved filenames (without full path) for database storage
     * @throws RuntimeException if file operations fail
     */
    public List<String> saveImages(List<MultipartFile> images) throws IOException, InvalidImageException {
        if (images == null || images.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> savedFilenames = new ArrayList<>();
        
        for (MultipartFile image : images) {
            if (image != null && !image.isEmpty()) {
                // Validate the image file
                if (!validateImageFile(image)) {
                    logger.warn("Invalid image file rejected: {}", image.getOriginalFilename());
                    throw new InvalidImageException("image is not valid " + image.getOriginalFilename());
                }

                BufferedImage bufferedImage;
                try(InputStream is = image.getInputStream())
                {
                    bufferedImage = ImageIO.read(is);
                }

                // Generate unique filename
                String filename = generateUniqueFilename(image.getOriginalFilename(), "webp");

                byte[] webpBytes;
                try(ByteArrayOutputStream baos = new ByteArrayOutputStream())
                {
                    ImageIO.write(bufferedImage, "webp", baos);
                    webpBytes = baos.toByteArray();
                }
                
                // Save the file
                Path targetPath = Paths.get(imageStoragePath, filename);
                FileUtils.writeByteArrayToFile(targetPath.toFile(), webpBytes);
                
                savedFilenames.add(filename);
                logger.info("Successfully saved image: {}", filename);
            }
        }
        
        return savedFilenames;
    }

    /**
     * Gets the full file system path to an image file.
     * 
     * @param filename The filename of the image (as stored in database)
     * @return Full path to the image file
     */
    public String getImagePath(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return null;
        }
        
        Path imagePath = Paths.get(imageStoragePath, filename);
        return imagePath.toString();
    }

    /**
     * Validates an uploaded image file.
     * 
     * Validation checks:
     * - File is not null and not empty
     * - File size is within limits
     * - File extension is supported
     * - Content type is an image type
     * 
     * @param file The MultipartFile to validate
     * @return true if the file is valid, false otherwise
     */
    public boolean validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            logger.debug("File is null or empty");
            return false;
        }

        // Check file size
        if (file.getSize() > maxFileSize) {
            logger.debug("File size {} exceeds maximum allowed size {}", file.getSize(), maxFileSize);
            return false;
        }

        // Check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            logger.debug("Original filename is null or empty");
            return false;
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!SUPPORTED_IMAGE_EXTENSIONS.contains(extension)) {
            logger.debug("Unsupported file extension: {}", extension);
            return false;
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            logger.debug("Invalid content type: {}", contentType);
            return false;
        }

        return true;
    }

    /**
     * Generates a unique filename to prevent conflicts.
     * 
     * @param originalFilename The original filename from the uploaded file
     * @return A unique filename with UUID prefix
     */
    private String generateUniqueFilename(String originalFilename, String extension) {
        // String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + extension;
    }

    /**
     * Extracts the file extension from a filename.
     * 
     * @param filename The filename to extract extension from
     * @return The file extension (without the dot)
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        
        return filename.substring(lastDotIndex + 1);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        File file = new File(imageStoragePath);
        if(!file.isDirectory() || !file.canWrite() || !.file.canRead())
        {
            throw new IllegalArgumentException("image storage path problem " + imageStoragePath);
        }
    }
}