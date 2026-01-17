package cx.ksg.notificationserver.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ImageService.
 * 
 * Tests cover:
 * - Image file validation (file types, sizes, content types)
 * - Image saving functionality
 * - Path generation and retrieval
 * - Directory creation
 * - Error handling scenarios
 * 
 * Uses temporary directories to avoid affecting the file system.
 */
class ImageServiceTest {

    private ImageService imageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        imageService = new ImageService();
        // Set the image storage path to our temp directory
        ReflectionTestUtils.setField(imageService, "imageStoragePath", tempDir.toString());
    }

    @Test
    void validateImageFile_ValidJpegFile_ReturnsTrue() {
        // Arrange
        MockMultipartFile validImage = new MockMultipartFile(
            "image", 
            "test.jpg", 
            "image/jpeg", 
            "fake image content".getBytes()
        );

        // Act
        boolean result = imageService.validateImageFile(validImage);

        // Assert
        assertTrue(result);
    }

    @Test
    void validateImageFile_ValidPngFile_ReturnsTrue() {
        // Arrange
        MockMultipartFile validImage = new MockMultipartFile(
            "image", 
            "test.png", 
            "image/png", 
            "fake image content".getBytes()
        );

        // Act
        boolean result = imageService.validateImageFile(validImage);

        // Assert
        assertTrue(result);
    }

    @Test
    void validateImageFile_NullFile_ReturnsFalse() {
        // Act
        boolean result = imageService.validateImageFile(null);

        // Assert
        assertFalse(result);
    }

    @Test
    void validateImageFile_EmptyFile_ReturnsFalse() {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
            "image", 
            "test.jpg", 
            "image/jpeg", 
            new byte[0]
        );

        // Act
        boolean result = imageService.validateImageFile(emptyFile);

        // Assert
        assertFalse(result);
    }

    @Test
    void validateImageFile_UnsupportedExtension_ReturnsFalse() {
        // Arrange
        MockMultipartFile invalidFile = new MockMultipartFile(
            "image", 
            "test.txt", 
            "text/plain", 
            "not an image".getBytes()
        );

        // Act
        boolean result = imageService.validateImageFile(invalidFile);

        // Assert
        assertFalse(result);
    }

    @Test
    void validateImageFile_InvalidContentType_ReturnsFalse() {
        // Arrange
        MockMultipartFile invalidFile = new MockMultipartFile(
            "image", 
            "test.jpg", 
            "text/plain", 
            "fake content".getBytes()
        );

        // Act
        boolean result = imageService.validateImageFile(invalidFile);

        // Assert
        assertFalse(result);
    }

    @Test
    void validateImageFile_NullOriginalFilename_ReturnsFalse() {
        // Arrange
        MockMultipartFile invalidFile = new MockMultipartFile(
            "image", 
            null, 
            "image/jpeg", 
            "fake content".getBytes()
        );

        // Act
        boolean result = imageService.validateImageFile(invalidFile);

        // Assert
        assertFalse(result);
    }

    @Test
    void validateImageFile_EmptyOriginalFilename_ReturnsFalse() {
        // Arrange
        MockMultipartFile invalidFile = new MockMultipartFile(
            "image", 
            "", 
            "image/jpeg", 
            "fake content".getBytes()
        );

        // Act
        boolean result = imageService.validateImageFile(invalidFile);

        // Assert
        assertFalse(result);
    }

    @Test
    void validateImageFile_FileTooLarge_ReturnsFalse() {
        // Arrange - Create a file larger than 10MB
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile largeFile = new MockMultipartFile(
            "image", 
            "large.jpg", 
            "image/jpeg", 
            largeContent
        );

        // Act
        boolean result = imageService.validateImageFile(largeFile);

        // Assert
        assertFalse(result);
    }

    @Test
    void saveImages_ValidImages_SavesSuccessfully() throws IOException {
        // Arrange
        MockMultipartFile image1 = new MockMultipartFile(
            "image1", 
            "test1.jpg", 
            "image/jpeg", 
            "fake image content 1".getBytes()
        );
        MockMultipartFile image2 = new MockMultipartFile(
            "image2", 
            "test2.png", 
            "image/png", 
            "fake image content 2".getBytes()
        );
        List<MultipartFile> images = Arrays.asList(image1, image2);

        // Act
        List<String> savedFilenames = imageService.saveImages(images);

        // Assert
        assertEquals(2, savedFilenames.size());
        
        // Verify files were actually saved
        for (String filename : savedFilenames) {
            Path savedFile = tempDir.resolve(filename);
            assertTrue(Files.exists(savedFile));
            assertTrue(Files.isRegularFile(savedFile));
        }
        
        // Verify filenames are unique (contain UUID)
        assertNotEquals(savedFilenames.get(0), savedFilenames.get(1));
        assertTrue(savedFilenames.get(0).endsWith(".jpg"));
        assertTrue(savedFilenames.get(1).endsWith(".png"));
    }

    @Test
    void saveImages_EmptyList_ReturnsEmptyList() {
        // Act
        List<String> result = imageService.saveImages(Collections.emptyList());

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void saveImages_NullList_ReturnsEmptyList() {
        // Act
        List<String> result = imageService.saveImages(null);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void saveImages_MixedValidAndInvalidFiles_SavesOnlyValid() {
        // Arrange
        MockMultipartFile validImage = new MockMultipartFile(
            "valid", 
            "valid.jpg", 
            "image/jpeg", 
            "valid content".getBytes()
        );
        MockMultipartFile invalidImage = new MockMultipartFile(
            "invalid", 
            "invalid.txt", 
            "text/plain", 
            "invalid content".getBytes()
        );
        List<MultipartFile> images = Arrays.asList(validImage, invalidImage);

        // Act
        List<String> savedFilenames = imageService.saveImages(images);

        // Assert
        assertEquals(1, savedFilenames.size());
        assertTrue(savedFilenames.get(0).endsWith(".jpg"));
        
        // Verify only the valid file was saved
        Path savedFile = tempDir.resolve(savedFilenames.get(0));
        assertTrue(Files.exists(savedFile));
    }

    @Test
    void saveImages_WithNullFiles_SkipsNullFiles() {
        // Arrange
        MockMultipartFile validImage = new MockMultipartFile(
            "valid", 
            "valid.jpg", 
            "image/jpeg", 
            "valid content".getBytes()
        );
        List<MultipartFile> images = Arrays.asList(validImage, null);

        // Act
        List<String> savedFilenames = imageService.saveImages(images);

        // Assert
        assertEquals(1, savedFilenames.size());
        assertTrue(savedFilenames.get(0).endsWith(".jpg"));
    }

    @Test
    void getImagePath_ValidFilename_ReturnsFullPath() {
        // Arrange
        String filename = "test-image.jpg";

        // Act
        String result = imageService.getImagePath(filename);

        // Assert
        String expected = Paths.get(tempDir.toString(), filename).toString();
        assertEquals(expected, result);
    }

    @Test
    void getImagePath_NullFilename_ReturnsNull() {
        // Act
        String result = imageService.getImagePath(null);

        // Assert
        assertNull(result);
    }

    @Test
    void getImagePath_EmptyFilename_ReturnsNull() {
        // Act
        String result = imageService.getImagePath("");

        // Assert
        assertNull(result);
    }

    @Test
    void getImagePath_WhitespaceFilename_ReturnsNull() {
        // Act
        String result = imageService.getImagePath("   ");

        // Assert
        assertNull(result);
    }

    @Test
    void saveImages_CreatesDirectoryIfNotExists() throws IOException {
        // Arrange - Use a subdirectory that doesn't exist yet
        Path nonExistentDir = tempDir.resolve("new-directory");
        ReflectionTestUtils.setField(imageService, "imageStoragePath", nonExistentDir.toString());
        
        MockMultipartFile validImage = new MockMultipartFile(
            "image", 
            "test.jpg", 
            "image/jpeg", 
            "content".getBytes()
        );

        // Act
        List<String> savedFilenames = imageService.saveImages(Arrays.asList(validImage));

        // Assert
        assertEquals(1, savedFilenames.size());
        assertTrue(Files.exists(nonExistentDir));
        assertTrue(Files.isDirectory(nonExistentDir));
        
        Path savedFile = nonExistentDir.resolve(savedFilenames.get(0));
        assertTrue(Files.exists(savedFile));
    }

    @Test
    void validateImageFile_SupportedExtensions_ReturnsTrue() {
        // Test all supported extensions
        String[] supportedExtensions = {"jpg", "jpeg", "png", "gif", "bmp", "webp"};
        
        for (String extension : supportedExtensions) {
            MockMultipartFile file = new MockMultipartFile(
                "image", 
                "test." + extension, 
                "image/" + extension, 
                "content".getBytes()
            );
            
            assertTrue(imageService.validateImageFile(file), 
                "Extension " + extension + " should be supported");
        }
    }

    @Test
    void validateImageFile_CaseInsensitiveExtensions_ReturnsTrue() {
        // Test case insensitive extension handling
        MockMultipartFile file = new MockMultipartFile(
            "image", 
            "test.JPG", 
            "image/jpeg", 
            "content".getBytes()
        );
        
        assertTrue(imageService.validateImageFile(file));
    }
}