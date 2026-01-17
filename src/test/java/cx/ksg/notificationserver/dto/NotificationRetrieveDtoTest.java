package cx.ksg.notificationserver.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NotificationRetrieveDto validation and functionality.
 */
class NotificationRetrieveDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidDto_AllFieldsValid() {
        // Given
        NotificationRetrieveDto dto = new NotificationRetrieveDto(10, 0, 1640995200L, 1641081600L);

        // When
        Set<ConstraintViolation<NotificationRetrieveDto>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty(), "Valid DTO should have no validation violations");
    }

    @Test
    void testValidDto_AllFieldsNull() {
        // Given - all fields are optional
        NotificationRetrieveDto dto = new NotificationRetrieveDto();

        // When
        Set<ConstraintViolation<NotificationRetrieveDto>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty(), "DTO with all null fields should be valid since all fields are optional");
    }

    @Test
    void testValidDto_OnlyLimitSet() {
        // Given
        NotificationRetrieveDto dto = new NotificationRetrieveDto();
        dto.setLimit(5);

        // When
        Set<ConstraintViolation<NotificationRetrieveDto>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty(), "DTO with only valid limit should be valid");
    }

    @Test
    void testValidDto_OnlyOffsetSet() {
        // Given
        NotificationRetrieveDto dto = new NotificationRetrieveDto();
        dto.setOffset(10);

        // When
        Set<ConstraintViolation<NotificationRetrieveDto>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty(), "DTO with only valid offset should be valid");
    }

    @Test
    void testInvalidLimit_Zero() {
        // Given
        NotificationRetrieveDto dto = new NotificationRetrieveDto();
        dto.setLimit(0);

        // When
        Set<ConstraintViolation<NotificationRetrieveDto>> violations = validator.validate(dto);

        // Then
        assertEquals(1, violations.size(), "Limit of 0 should cause validation violation");
        ConstraintViolation<NotificationRetrieveDto> violation = violations.iterator().next();
        assertEquals("Limit must be at least 1", violation.getMessage());
        assertEquals("limit", violation.getPropertyPath().toString());
    }

    @Test
    void testInvalidLimit_Negative() {
        // Given
        NotificationRetrieveDto dto = new NotificationRetrieveDto();
        dto.setLimit(-5);

        // When
        Set<ConstraintViolation<NotificationRetrieveDto>> violations = validator.validate(dto);

        // Then
        assertEquals(1, violations.size(), "Negative limit should cause validation violation");
        ConstraintViolation<NotificationRetrieveDto> violation = violations.iterator().next();
        assertEquals("Limit must be at least 1", violation.getMessage());
        assertEquals("limit", violation.getPropertyPath().toString());
    }

    @Test
    void testInvalidOffset_Negative() {
        // Given
        NotificationRetrieveDto dto = new NotificationRetrieveDto();
        dto.setOffset(-1);

        // When
        Set<ConstraintViolation<NotificationRetrieveDto>> violations = validator.validate(dto);

        // Then
        assertEquals(1, violations.size(), "Negative offset should cause validation violation");
        ConstraintViolation<NotificationRetrieveDto> violation = violations.iterator().next();
        assertEquals("Offset must be non-negative", violation.getMessage());
        assertEquals("offset", violation.getPropertyPath().toString());
    }

    @Test
    void testValidOffset_Zero() {
        // Given
        NotificationRetrieveDto dto = new NotificationRetrieveDto();
        dto.setOffset(0);

        // When
        Set<ConstraintViolation<NotificationRetrieveDto>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty(), "Offset of 0 should be valid");
    }

    @Test
    void testMultipleValidationErrors() {
        // Given
        NotificationRetrieveDto dto = new NotificationRetrieveDto();
        dto.setLimit(-1);
        dto.setOffset(-5);

        // When
        Set<ConstraintViolation<NotificationRetrieveDto>> violations = validator.validate(dto);

        // Then
        assertEquals(2, violations.size(), "Should have violations for both limit and offset");
    }

    @Test
    void testTimestampFields_ValidValues() {
        // Given
        NotificationRetrieveDto dto = new NotificationRetrieveDto();
        dto.setFromTimestamp(1640995200L); // Jan 1, 2022
        dto.setToTimestamp(1641081600L);   // Jan 2, 2022

        // When
        Set<ConstraintViolation<NotificationRetrieveDto>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty(), "Valid timestamps should not cause violations");
    }

    @Test
    void testTimestampFields_NullValues() {
        // Given
        NotificationRetrieveDto dto = new NotificationRetrieveDto();
        dto.setFromTimestamp(null);
        dto.setToTimestamp(null);

        // When
        Set<ConstraintViolation<NotificationRetrieveDto>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty(), "Null timestamps should be valid since they are optional");
    }

    @Test
    void testConstructorWithAllFields() {
        // Given
        Integer limit = 20;
        Integer offset = 5;
        Long fromTimestamp = 1640995200L;
        Long toTimestamp = 1641081600L;

        // When
        NotificationRetrieveDto dto = new NotificationRetrieveDto(limit, offset, fromTimestamp, toTimestamp);

        // Then
        assertEquals(limit, dto.getLimit());
        assertEquals(offset, dto.getOffset());
        assertEquals(fromTimestamp, dto.getFromTimestamp());
        assertEquals(toTimestamp, dto.getToTimestamp());
    }

    @Test
    void testDefaultConstructor() {
        // When
        NotificationRetrieveDto dto = new NotificationRetrieveDto();

        // Then
        assertNull(dto.getLimit());
        assertNull(dto.getOffset());
        assertNull(dto.getFromTimestamp());
        assertNull(dto.getToTimestamp());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        NotificationRetrieveDto dto = new NotificationRetrieveDto();

        // When
        dto.setLimit(15);
        dto.setOffset(3);
        dto.setFromTimestamp(1640995200L);
        dto.setToTimestamp(1641081600L);

        // Then
        assertEquals(15, dto.getLimit());
        assertEquals(3, dto.getOffset());
        assertEquals(1640995200L, dto.getFromTimestamp());
        assertEquals(1641081600L, dto.getToTimestamp());
    }

    @Test
    void testToString() {
        // Given
        NotificationRetrieveDto dto = new NotificationRetrieveDto(10, 5, 1640995200L, 1641081600L);

        // When
        String result = dto.toString();

        // Then
        assertTrue(result.contains("limit=10"));
        assertTrue(result.contains("offset=5"));
        assertTrue(result.contains("fromTimestamp=1640995200"));
        assertTrue(result.contains("toTimestamp=1641081600"));
        assertTrue(result.startsWith("NotificationRetrieveDto{"));
    }

    @Test
    void testToString_NullValues() {
        // Given
        NotificationRetrieveDto dto = new NotificationRetrieveDto();

        // When
        String result = dto.toString();

        // Then
        assertTrue(result.contains("limit=null"));
        assertTrue(result.contains("offset=null"));
        assertTrue(result.contains("fromTimestamp=null"));
        assertTrue(result.contains("toTimestamp=null"));
    }
}