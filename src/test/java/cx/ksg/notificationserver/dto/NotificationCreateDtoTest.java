package cx.ksg.notificationserver.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NotificationCreateDto validation and functionality.
 */
class NotificationCreateDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidNotificationCreateDto() {
        // Given
        NotificationCreateDto dto = new NotificationCreateDto(
                "Test notification content",
                System.currentTimeMillis(),
                "test-sender",
                Arrays.asList("image1.jpg", "image2.png")
        );

        // When
        Set<ConstraintViolation<NotificationCreateDto>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty(), "Valid DTO should have no validation violations");
    }

    @Test
    void testValidNotificationCreateDtoWithoutImages() {
        // Given
        NotificationCreateDto dto = new NotificationCreateDto(
                "Test notification content",
                System.currentTimeMillis(),
                "test-sender",
                null
        );

        // When
        Set<ConstraintViolation<NotificationCreateDto>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty(), "Valid DTO without images should have no validation violations");
    }

    @Test
    void testBlankContentValidation() {
        // Given
        NotificationCreateDto dto = new NotificationCreateDto(
                "",
                System.currentTimeMillis(),
                "test-sender",
                null
        );

        // When
        Set<ConstraintViolation<NotificationCreateDto>> violations = validator.validate(dto);

        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<NotificationCreateDto> violation = violations.iterator().next();
        assertEquals("content", violation.getPropertyPath().toString());
        assertEquals("Content is required and cannot be blank", violation.getMessage());
    }

    @Test
    void testNullContentValidation() {
        // Given
        NotificationCreateDto dto = new NotificationCreateDto(
                null,
                System.currentTimeMillis(),
                "test-sender",
                null
        );

        // When
        Set<ConstraintViolation<NotificationCreateDto>> violations = validator.validate(dto);

        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<NotificationCreateDto> violation = violations.iterator().next();
        assertEquals("content", violation.getPropertyPath().toString());
        assertEquals("Content is required and cannot be blank", violation.getMessage());
    }

    @Test
    void testContentTooLongValidation() {
        // Given
        String longContent = "a".repeat(10001); // Exceeds 10000 character limit
        NotificationCreateDto dto = new NotificationCreateDto(
                longContent,
                System.currentTimeMillis(),
                "test-sender",
                null
        );

        // When
        Set<ConstraintViolation<NotificationCreateDto>> violations = validator.validate(dto);

        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<NotificationCreateDto> violation = violations.iterator().next();
        assertEquals("content", violation.getPropertyPath().toString());
        assertEquals("Content cannot exceed 10000 characters", violation.getMessage());
    }

    @Test
    void testNullSendOnValidation() {
        // Given
        NotificationCreateDto dto = new NotificationCreateDto(
                "Test content",
                null,
                "test-sender",
                null
        );

        // When
        Set<ConstraintViolation<NotificationCreateDto>> violations = validator.validate(dto);

        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<NotificationCreateDto> violation = violations.iterator().next();
        assertEquals("sendOn", violation.getPropertyPath().toString());
        assertEquals("Send timestamp (sendOn) is required", violation.getMessage());
    }

    @Test
    void testBlankFromValidation() {
        // Given
        NotificationCreateDto dto = new NotificationCreateDto(
                "Test content",
                System.currentTimeMillis(),
                "",
                null
        );

        // When
        Set<ConstraintViolation<NotificationCreateDto>> violations = validator.validate(dto);

        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<NotificationCreateDto> violation = violations.iterator().next();
        assertEquals("from", violation.getPropertyPath().toString());
        assertEquals("From field is required and cannot be blank", violation.getMessage());
    }

    @Test
    void testNullFromValidation() {
        // Given
        NotificationCreateDto dto = new NotificationCreateDto(
                "Test content",
                System.currentTimeMillis(),
                null,
                null
        );

        // When
        Set<ConstraintViolation<NotificationCreateDto>> violations = validator.validate(dto);

        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<NotificationCreateDto> violation = violations.iterator().next();
        assertEquals("from", violation.getPropertyPath().toString());
        assertEquals("From field is required and cannot be blank", violation.getMessage());
    }

    @Test
    void testMultipleValidationErrors() {
        // Given
        NotificationCreateDto dto = new NotificationCreateDto(
                null,
                null,
                null,
                null
        );

        // When
        Set<ConstraintViolation<NotificationCreateDto>> violations = validator.validate(dto);

        // Then
        assertEquals(3, violations.size(), "Should have violations for content, sendOn, and from fields");
    }

    @Test
    void testGettersAndSetters() {
        // Given
        NotificationCreateDto dto = new NotificationCreateDto();
        String content = "Test content";
        Long sendOn = System.currentTimeMillis();
        String from = "test-sender";
        List<String> images = Arrays.asList("image1.jpg", "image2.png");

        // When
        dto.setContent(content);
        dto.setSendOn(sendOn);
        dto.setFrom(from);
        dto.setImages(images);

        // Then
        assertEquals(content, dto.getContent());
        assertEquals(sendOn, dto.getSendOn());
        assertEquals(from, dto.getFrom());
        assertEquals(images, dto.getImages());
    }

    @Test
    void testToString() {
        // Given
        NotificationCreateDto dto = new NotificationCreateDto(
                "Test content",
                1234567890L,
                "test-sender",
                Arrays.asList("image1.jpg")
        );

        // When
        String result = dto.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Test content"));
        assertTrue(result.contains("1234567890"));
        assertTrue(result.contains("test-sender"));
        assertTrue(result.contains("image1.jpg"));
    }
}