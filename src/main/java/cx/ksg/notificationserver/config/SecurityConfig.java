package cx.ksg.notificationserver.config;

import cx.ksg.notificationserver.security.BearerTokenAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for the notification system.
 * Configures Bearer token authentication for API endpoints while excluding health check endpoints.
 * 
 * Security requirements:
 * - All /notification/* endpoints require Bearer token authentication
 * - Health check endpoints (/healthcheck, /actuator/health*) are excluded from authentication
 * - CSRF is disabled for API usage
 * - Stateless session management for REST API
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final BearerTokenAuthenticationFilter bearerTokenAuthenticationFilter;

    @Autowired
    public SecurityConfig(BearerTokenAuthenticationFilter bearerTokenAuthenticationFilter) {
        this.bearerTokenAuthenticationFilter = bearerTokenAuthenticationFilter;
    }

    /**
     * Configures the security filter chain for HTTP requests.
     * 
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for REST API usage
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configure session management to be stateless
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Configure authorization rules
            .authorizeHttpRequests(authz -> authz
                // Allow health check endpoints without authentication
                .requestMatchers("/healthcheck").permitAll()

                .requestMatchers("/image/**").authenticated()
                
                // Require authentication for all notification endpoints
                .requestMatchers("/notification/**").authenticated()
                
                // Require authentication for all other requests by default
                .anyRequest().authenticated()
            )
            
            // Add the Bearer token authentication filter before the default authentication filter
            .addFilterBefore(bearerTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // Disable form login since we're using Bearer token authentication
            .formLogin(AbstractHttpConfigurer::disable)
            
            // Disable HTTP Basic authentication
            .httpBasic(AbstractHttpConfigurer::disable)

            // disable csrf
            .csrf(x -> x.disable())

            ;
        return http.build();
    }
}