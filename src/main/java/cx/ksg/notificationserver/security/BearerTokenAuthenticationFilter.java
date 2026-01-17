package cx.ksg.notificationserver.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Collections;

/**
 * Filter to validate Bearer token authentication for API endpoints.
 * Validates Authorization header with "Bearer XXXXXXXX" format against configured token.
 * Sets Spring Security authentication context for valid tokens.
 * Returns 401 Unauthorized for missing or invalid tokens.
 */
@Component
public class BearerTokenAuthenticationFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(BearerTokenAuthenticationFilter.class);
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    @Value("${notification.auth.bearer-token}")
    private String configuredBearerToken;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestPath = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        
        logger.debug("Processing request: {} {}", method, requestPath);
        
        // Skip authentication for health check endpoint
        if (isHealthCheckEndpoint(requestPath)) {
            logger.debug("Skipping authentication for health check endpoint");
            chain.doFilter(request, response);
            return;
        }
        
        // Extract Bearer token from Authorization header
        String authorizationHeader = httpRequest.getHeader(AUTHORIZATION_HEADER);
        
        if (!StringUtils.hasText(authorizationHeader)) {
            logger.debug("Missing Authorization header");
            sendUnauthorizedResponse(httpResponse, "Missing Authorization header");
            return;
        }
        
        if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
            logger.debug("Authorization header does not start with 'Bearer '");
            sendUnauthorizedResponse(httpResponse, "Invalid Authorization header format. Expected 'Bearer <token>'");
            return;
        }
        
        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        
        if (!StringUtils.hasText(token)) {
            logger.debug("Empty Bearer token");
            sendUnauthorizedResponse(httpResponse, "Empty Bearer token");
            return;
        }
        
        // Validate Bearer token
        if (!validateBearerToken(token)) {
            logger.debug("Invalid Bearer token");
            sendUnauthorizedResponse(httpResponse, "Invalid Bearer token");
            return;
        }
        
        // Set authentication context for valid token
        setAuthenticationContext();
        
        logger.debug("Bearer token validated successfully");
        
        // Continue with the filter chain
        chain.doFilter(request, response);
    }
    
    /**
     * Validates the Bearer token against the configured token.
     * 
     * @param token the Bearer token to validate
     * @return true if the token is valid, false otherwise
     */
    private boolean validateBearerToken(String token) {
        return configuredBearerToken != null && configuredBearerToken.equals(token);
    }
    
    /**
     * Sets the Spring Security authentication context for a valid token.
     */
    private void setAuthenticationContext() {
        // Create a simple authentication object with a granted authority
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            "api-client", // principal
            null, // credentials (not needed for Bearer token)
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_API_CLIENT"))
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    
    /**
     * Checks if the request path is for the health check endpoint.
     * 
     * @param requestPath the request path
     * @return true if it's a health check endpoint, false otherwise
     */
    private boolean isHealthCheckEndpoint(String requestPath) {
        return "/healthcheck".equals(requestPath) || 
               requestPath.startsWith("/actuator/health");
    }
    
    /**
     * Sends a 401 Unauthorized response with error details.
     * 
     * @param response the HTTP response
     * @param message the error message
     * @throws IOException if an I/O error occurs
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String errorResponse = String.format(
            "{\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"%s\",\"timestamp\":\"%s\"}}",
            message,
            java.time.Instant.now().toString()
        );
        
        response.getWriter().write(errorResponse);
        response.getWriter().flush();
    }
}