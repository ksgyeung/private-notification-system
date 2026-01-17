package cx.ksg.notificationserver.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for HealthController with full security configuration.
 * 
 * Tests that the health check endpoint works correctly when the full
 * Spring Security configuration is loaded, ensuring it's properly excluded
 * from authentication requirements.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HealthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthCheck_WithFullSecurityConfig_ReturnsOkWithoutAuthentication() throws Exception {
        // When & Then - Should work without authentication even with full security config
        mockMvc.perform(get("/healthcheck"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string("ok"));
    }

    @Test
    void healthCheck_WithFullSecurityConfig_ReturnsHttp200() throws Exception {
        // When & Then
        mockMvc.perform(get("/healthcheck"))
                .andExpect(status().is(200))
                .andExpect(content().string("ok"));
    }
}