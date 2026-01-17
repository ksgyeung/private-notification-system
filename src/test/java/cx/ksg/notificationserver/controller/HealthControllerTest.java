package cx.ksg.notificationserver.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for HealthController.
 * 
 * Tests the health check endpoint to ensure it returns the correct response
 * for Docker container monitoring and system health verification.
 */
@WebMvcTest(value = HealthController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthCheck_ReturnsOkWithPlainText() throws Exception {
        // When & Then
        mockMvc.perform(get("/healthcheck"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string("ok"));
    }

    @Test
    void healthCheck_NoAuthenticationRequired() throws Exception {
        // When & Then - Should work without any authentication headers
        mockMvc.perform(get("/healthcheck"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    void healthCheck_ReturnsHttp200() throws Exception {
        // When & Then
        mockMvc.perform(get("/healthcheck"))
                .andExpect(status().is(200))
                .andExpect(content().string("ok"));
    }
}