package cx.ksg.notificationserver.controller;

import cx.ksg.notificationserver.dto.NotificationCreateDto;
import cx.ksg.notificationserver.dto.NotificationListResponseDto;
import cx.ksg.notificationserver.dto.NotificationResponseDto;
import cx.ksg.notificationserver.dto.NotificationRetrieveDto;
import cx.ksg.notificationserver.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for NotificationController.
 * 
 * Tests the REST endpoints for notification creation and retrieval,
 * including validation, error handling, and proper HTTP status codes.
 */
@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    private NotificationCreateDto validCreateRequest;
    private NotificationResponseDto mockResponse;
    private NotificationRetrieveDto validRetrieveRequest;
    private NotificationListResponseDto mockListResponse;

    @BeforeEach
    void setUp() {
        // Set up valid create request
        validCreateRequest = new NotificationCreateDto();
        validCreateRequest.setContent("Test notification content");
        validCreateRequest.setSendOn(System.currentTimeMillis() / 1000);
        validCreateRequest.setFrom("test-sender");
        validCreateRequest.setImages(Arrays.asList("image1.jpg", "image2.png"));

        // Set up mock response
        mockResponse = new NotificationResponseDto();
        mockResponse.setId(1L);
        mockResponse.setContent("Test notification content");
        mockResponse.setSendOn(System.currentTimeMillis() / 1000);
        mockResponse.setFrom("test-sender");
        mockResponse.setImages(Arrays.asList("image1.jpg", "image2.png"));

        // Set up valid retrieve request
        validRetrieveRequest = new NotificationRetrieveDto();
        validRetrieveRequest.setLimit(10);
        validRetrieveRequest.setOffset(0);

        // Set up mock list response
        mockListResponse = new NotificationListResponseDto();
        mockListResponse.setNotifications(Collections.singletonList(mockResponse));
        mockListResponse.setCount(1);
        mockListResponse.setLimit(10);
        mockListResponse.setOffset(0);
    }

    @Test
    void createNotification_ValidRequest_ReturnsCreated() throws Exception {
        // Given
        when(notificationService.createNotification(any(NotificationCreateDto.class)))
            .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/notification/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("Test notification content"))
                .andExpect(jsonPath("$.from").value("test-sender"))
                .andExpect(jsonPath("$.images[0]").value("image1.jpg"))
                .andExpect(jsonPath("$.images[1]").value("image2.png"));

        verify(notificationService, times(1)).createNotification(any(NotificationCreateDto.class));
    }

    @Test
    void createNotification_MissingContent_ReturnsBadRequest() throws Exception {
        // Given
        NotificationCreateDto invalidRequest = new NotificationCreateDto();
        invalidRequest.setSendOn(System.currentTimeMillis() / 1000);
        invalidRequest.setFrom("test-sender");
        // Missing content

        // When & Then
        mockMvc.perform(post("/notification/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.message").value("Request validation failed"));

        verify(notificationService, never()).createNotification(any(NotificationCreateDto.class));
    }

    @Test
    void createNotification_MissingFrom_ReturnsBadRequest() throws Exception {
        // Given
        NotificationCreateDto invalidRequest = new NotificationCreateDto();
        invalidRequest.setContent("Test content");
        invalidRequest.setSendOn(System.currentTimeMillis() / 1000);
        // Missing from

        // When & Then
        mockMvc.perform(post("/notification/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        verify(notificationService, never()).createNotification(any(NotificationCreateDto.class));
    }

    @Test
    void createNotification_MissingSendOn_ReturnsBadRequest() throws Exception {
        // Given
        NotificationCreateDto invalidRequest = new NotificationCreateDto();
        invalidRequest.setContent("Test content");
        invalidRequest.setFrom("test-sender");
        // Missing sendOn

        // When & Then
        mockMvc.perform(post("/notification/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        verify(notificationService, never()).createNotification(any(NotificationCreateDto.class));
    }

    @Test
    void createNotification_ContentTooLong_ReturnsBadRequest() throws Exception {
        // Given
        NotificationCreateDto invalidRequest = new NotificationCreateDto();
        invalidRequest.setContent("a".repeat(10001)); // Exceeds 10000 character limit
        invalidRequest.setSendOn(System.currentTimeMillis() / 1000);
        invalidRequest.setFrom("test-sender");

        // When & Then
        mockMvc.perform(post("/notification/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        verify(notificationService, never()).createNotification(any(NotificationCreateDto.class));
    }

    @Test
    void createNotification_ServiceThrowsException_ReturnsInternalServerError() throws Exception {
        // Given
        when(notificationService.createNotification(any(NotificationCreateDto.class)))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(post("/notification/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error.code").value("NOTIFICATION_CREATION_FAILED"))
                .andExpect(jsonPath("$.error.path").value("/notification/create"));

        verify(notificationService, times(1)).createNotification(any(NotificationCreateDto.class));
    }

    @Test
    void retrieveNotifications_ValidRequest_ReturnsOk() throws Exception {
        // Given
        when(notificationService.retrieveNotifications(any(NotificationRetrieveDto.class)))
            .thenReturn(mockListResponse);

        // When & Then
        mockMvc.perform(post("/notification/retrieve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRetrieveRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.limit").value(10))
                .andExpect(jsonPath("$.offset").value(0))
                .andExpect(jsonPath("$.notifications[0].id").value(1L))
                .andExpect(jsonPath("$.notifications[0].content").value("Test notification content"));

        verify(notificationService, times(1)).retrieveNotifications(any(NotificationRetrieveDto.class));
    }

    @Test
    void retrieveNotifications_EmptyRequest_ReturnsOk() throws Exception {
        // Given
        NotificationRetrieveDto emptyRequest = new NotificationRetrieveDto();
        NotificationListResponseDto emptyResponse = new NotificationListResponseDto();
        emptyResponse.setNotifications(Collections.emptyList());
        emptyResponse.setCount(0);

        when(notificationService.retrieveNotifications(any(NotificationRetrieveDto.class)))
            .thenReturn(emptyResponse);

        // When & Then
        mockMvc.perform(post("/notification/retrieve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(0))
                .andExpect(jsonPath("$.notifications").isArray())
                .andExpect(jsonPath("$.notifications").isEmpty());

        verify(notificationService, times(1)).retrieveNotifications(any(NotificationRetrieveDto.class));
    }

    @Test
    void retrieveNotifications_InvalidLimit_ReturnsBadRequest() throws Exception {
        // Given
        NotificationRetrieveDto invalidRequest = new NotificationRetrieveDto();
        invalidRequest.setLimit(0); // Invalid - must be at least 1
        invalidRequest.setOffset(0);

        // When & Then
        mockMvc.perform(post("/notification/retrieve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        verify(notificationService, never()).retrieveNotifications(any(NotificationRetrieveDto.class));
    }

    @Test
    void retrieveNotifications_InvalidOffset_ReturnsBadRequest() throws Exception {
        // Given
        NotificationRetrieveDto invalidRequest = new NotificationRetrieveDto();
        invalidRequest.setLimit(10);
        invalidRequest.setOffset(-1); // Invalid - must be non-negative

        // When & Then
        mockMvc.perform(post("/notification/retrieve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        verify(notificationService, never()).retrieveNotifications(any(NotificationRetrieveDto.class));
    }

    @Test
    void retrieveNotifications_ServiceThrowsException_ReturnsInternalServerError() throws Exception {
        // Given
        when(notificationService.retrieveNotifications(any(NotificationRetrieveDto.class)))
            .thenThrow(new RuntimeException("Database query failed"));

        // When & Then
        mockMvc.perform(post("/notification/retrieve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRetrieveRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error.code").value("NOTIFICATION_RETRIEVAL_FAILED"))
                .andExpect(jsonPath("$.error.path").value("/notification/retrieve"));

        verify(notificationService, times(1)).retrieveNotifications(any(NotificationRetrieveDto.class));
    }

    @Test
    void createNotification_WithoutImages_ReturnsCreated() throws Exception {
        // Given
        NotificationCreateDto requestWithoutImages = new NotificationCreateDto();
        requestWithoutImages.setContent("Test notification without images");
        requestWithoutImages.setSendOn(System.currentTimeMillis() / 1000);
        requestWithoutImages.setFrom("test-sender");
        // No images

        NotificationResponseDto responseWithoutImages = new NotificationResponseDto();
        responseWithoutImages.setId(2L);
        responseWithoutImages.setContent("Test notification without images");
        responseWithoutImages.setSendOn(System.currentTimeMillis() / 1000);
        responseWithoutImages.setFrom("test-sender");
        responseWithoutImages.setImages(Collections.emptyList());

        when(notificationService.createNotification(any(NotificationCreateDto.class)))
            .thenReturn(responseWithoutImages);

        // When & Then
        mockMvc.perform(post("/notification/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestWithoutImages)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.content").value("Test notification without images"))
                .andExpect(jsonPath("$.images").isArray())
                .andExpect(jsonPath("$.images").isEmpty());

        verify(notificationService, times(1)).createNotification(any(NotificationCreateDto.class));
    }

    @Test
    void retrieveNotifications_WithTimestampFilters_ReturnsOk() throws Exception {
        // Given
        NotificationRetrieveDto requestWithFilters = new NotificationRetrieveDto();
        requestWithFilters.setFromTimestamp(1640995200L); // 2022-01-01 00:00:00 UTC
        requestWithFilters.setToTimestamp(1672531199L);   // 2022-12-31 23:59:59 UTC
        requestWithFilters.setLimit(5);
        requestWithFilters.setOffset(0);

        when(notificationService.retrieveNotifications(any(NotificationRetrieveDto.class)))
            .thenReturn(mockListResponse);

        // When & Then
        mockMvc.perform(post("/notification/retrieve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestWithFilters)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.notifications").isArray());

        verify(notificationService, times(1)).retrieveNotifications(any(NotificationRetrieveDto.class));
    }
}