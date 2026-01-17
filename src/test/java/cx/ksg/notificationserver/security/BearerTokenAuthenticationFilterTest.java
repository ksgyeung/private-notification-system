package cx.ksg.notificationserver.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BearerTokenAuthenticationFilterTest {

    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @Mock
    private FilterChain filterChain;
    
    private BearerTokenAuthenticationFilter filter;
    private StringWriter responseWriter;
    
    @BeforeEach
    void setUp() throws IOException {
        filter = new BearerTokenAuthenticationFilter();
        ReflectionTestUtils.setField(filter, "configuredBearerToken", "test-token");
        
        responseWriter = new StringWriter();
        
        // Clear security context before each test
        SecurityContextHolder.clearContext();
    }
    
    private void setupResponseWriter() throws IOException {
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);
    }
    
    @Test
    void doFilter_WithValidBearerToken_ShouldSetAuthenticationAndContinueChain() throws IOException, ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/notification/create");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
        
        // Act
        filter.doFilter(request, response, filterChain);
        
        // Assert
        verify(filterChain).doFilter(request, response);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("api-client", authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_API_CLIENT")));
    }
    
    @Test
    void doFilter_WithMissingAuthorizationHeader_ShouldReturn401() throws IOException, ServletException {
        // Arrange
        setupResponseWriter();
        when(request.getRequestURI()).thenReturn("/notification/create");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("Authorization")).thenReturn(null);
        
        // Act
        filter.doFilter(request, response, filterChain);
        
        // Assert
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        verify(filterChain, never()).doFilter(request, response);
        
        String responseContent = responseWriter.toString();
        assertTrue(responseContent.contains("Missing Authorization header"));
        assertTrue(responseContent.contains("UNAUTHORIZED"));
    }
    
    @Test
    void doFilter_WithInvalidBearerTokenFormat_ShouldReturn401() throws IOException, ServletException {
        // Arrange
        setupResponseWriter();
        when(request.getRequestURI()).thenReturn("/notification/create");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("Authorization")).thenReturn("Basic invalid-format");
        
        // Act
        filter.doFilter(request, response, filterChain);
        
        // Assert
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
        
        String responseContent = responseWriter.toString();
        assertTrue(responseContent.contains("Invalid Authorization header format"));
    }
    
    @Test
    void doFilter_WithEmptyBearerToken_ShouldReturn401() throws IOException, ServletException {
        // Arrange
        setupResponseWriter();
        when(request.getRequestURI()).thenReturn("/notification/create");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("Authorization")).thenReturn("Bearer ");
        
        // Act
        filter.doFilter(request, response, filterChain);
        
        // Assert
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
        
        String responseContent = responseWriter.toString();
        assertTrue(responseContent.contains("Empty Bearer token"));
    }
    
    @Test
    void doFilter_WithInvalidBearerToken_ShouldReturn401() throws IOException, ServletException {
        // Arrange
        setupResponseWriter();
        when(request.getRequestURI()).thenReturn("/notification/create");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        
        // Act
        filter.doFilter(request, response, filterChain);
        
        // Assert
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
        
        String responseContent = responseWriter.toString();
        assertTrue(responseContent.contains("Invalid Bearer token"));
    }
    
    @Test
    void doFilter_WithHealthCheckEndpoint_ShouldSkipAuthentication() throws IOException, ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/healthcheck");
        when(request.getMethod()).thenReturn("GET");
        
        // Act
        filter.doFilter(request, response, filterChain);
        
        // Assert
        verify(filterChain).doFilter(request, response);
        verify(request, never()).getHeader("Authorization");
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    
    @Test
    void doFilter_WithActuatorHealthEndpoint_ShouldSkipAuthentication() throws IOException, ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/actuator/health");
        when(request.getMethod()).thenReturn("GET");
        
        // Act
        filter.doFilter(request, response, filterChain);
        
        // Assert
        verify(filterChain).doFilter(request, response);
        verify(request, never()).getHeader("Authorization");
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    
    @Test
    void doFilter_WithBearerTokenContainingWhitespace_ShouldTrimAndValidate() throws IOException, ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/notification/create");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("Authorization")).thenReturn("Bearer   test-token   ");
        
        // Act
        filter.doFilter(request, response, filterChain);
        
        // Assert
        verify(filterChain).doFilter(request, response);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
    }
}