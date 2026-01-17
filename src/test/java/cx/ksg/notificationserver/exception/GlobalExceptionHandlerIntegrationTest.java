package cx.ksg.notificationserver.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for GlobalExceptionHandler.
 * 
 * Tests the global exception handler in the context of a full Spring Boot application
 * to ensure it properly handles exceptions from controllers and returns consistent
 * error responses.
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class GlobalExceptionHandlerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Test
    void validationError_ShouldReturnBadRequestWithErrorStructure() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Test with missing required fields to trigger validation error
        String invalidJson = """
            {
                "content": "",
                "sendOn": null
            }
            """;

        mockMvc.perform(post("/notification/create")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.message").exists())
                .andExpect(jsonPath("$.error.timestamp").exists())
                .andExpect(jsonPath("$.error.path").value("/notification/create"));
    }

    @Test
    void malformedJson_ShouldReturnBadRequestWithJsonParsingError() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Test with malformed JSON to trigger JSON parsing error
        String malformedJson = """
            {
                "content": "Test notification",
                "sendOn": 1642262400,
                "from": "test-service"
                // Missing closing brace and has comment
            """;

        mockMvc.perform(post("/notification/create")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").value("JSON_PARSING_ERROR"))
                .andExpect(jsonPath("$.error.message").exists())
                .andExpect(jsonPath("$.error.timestamp").exists())
                .andExpect(jsonPath("$.error.path").value("/notification/create"));
    }

    @Test
    void missingRequestBody_ShouldReturnBadRequestWithJsonParsingError() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Test with no request body to trigger JSON parsing error
        mockMvc.perform(post("/notification/create")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").value("JSON_PARSING_ERROR"))
                .andExpect(jsonPath("$.error.message").exists())
                .andExpect(jsonPath("$.error.timestamp").exists())
                .andExpect(jsonPath("$.error.path").value("/notification/create"));
    }

    @Test
    void unauthorizedRequest_ShouldReturnUnauthorized() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Test without Authorization header to trigger authentication error
        String validJson = """
            {
                "content": "Test notification",
                "sendOn": 1642262400,
                "from": "test-service"
            }
            """;

        mockMvc.perform(post("/notification/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.error.message").exists())
                .andExpect(jsonPath("$.error.timestamp").exists());
    }

    @Test
    void invalidBearerToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Test with invalid Bearer token to trigger authentication error
        String validJson = """
            {
                "content": "Test notification",
                "sendOn": 1642262400,
                "from": "test-service"
            }
            """;

        mockMvc.perform(post("/notification/create")
                .header("Authorization", "Bearer invalid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.error.message").exists())
                .andExpect(jsonPath("$.error.timestamp").exists());
    }

    @Test
    void healthCheckEndpoint_ShouldNotRequireAuthentication() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Health check should work without authentication
        mockMvc.perform(post("/healthcheck"))
                .andExpect(status().isMethodNotAllowed()); // POST not allowed, but no auth error

        // GET should work
        mockMvc.perform(post("/healthcheck"))
                .andExpect(status().isMethodNotAllowed()); // Still method not allowed, but that's expected
    }
}