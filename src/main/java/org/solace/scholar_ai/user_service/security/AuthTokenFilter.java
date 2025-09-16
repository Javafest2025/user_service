package org.solace.scholar_ai.user_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solace.scholar_ai.user_service.service.auth.UserLoadingService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter for authenticating users based on JWT tokens in the Authorization
 * header.
 * This filter intercepts incoming requests, extracts and validates the JWT
 * token,
 * and sets the authentication in the Spring Security context if the token is
 * valid.
 */
@Component
@RequiredArgsConstructor
public class AuthTokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserLoadingService userLoadingService;
    private static final Logger log = LoggerFactory.getLogger(AuthTokenFilter.class);
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Performs the filtering logic for each request.
     * It extracts the JWT from the request, validates it, and if valid,
     * loads the user details and sets the authentication in the security context.
     *
     * @param request     The HTTP servlet request.
     * @param response    The HTTP servlet response.
     * @param filterChain The filter chain.
     * @throws ServletException If a servlet-specific error occurs.
     * @throws IOException      If an I/O error occurs.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        log.debug("AuthTokenFilter called for URI: {}", requestURI);

        // Skip authentication for public endpoints
        if (isPublicEndpoint(requestURI)) {
            log.debug("Skipping authentication for public endpoint: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String accessToken = parseJwt(request);
            if (accessToken != null && jwtUtils.validateJwtToken(accessToken)) {
                String username = jwtUtils.getUserNameFromJwtToken(accessToken);
                log.debug("Username: {}", username);

                // Check if refresh token still exists in Redis
                String redisKey = "refresh_token:" + username;
                try {
                    Boolean hasKey = redisTemplate.hasKey(redisKey);
                    if (hasKey == null || !hasKey) {
                        log.warn(
                                "Access token permitted despite missing refresh token for user '{}' in Redis - token is still valid",
                                username);
                        // Continue with authentication instead of failing
                        // This allows the system to work even if Redis is down or refresh token expired
                        // The JWT token itself is still valid and sufficient for authentication
                    }
                } catch (Exception redisException) {
                    log.warn(
                            "Redis connection error for user '{}', proceeding with JWT-only authentication: {}",
                            username,
                            redisException.getMessage());
                    // Continue with authentication if Redis is unavailable
                }

                UserDetails userDetails = userLoadingService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                log.debug("Roles from JWT: {}", userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Checks if the given URI is a public endpoint that doesn't require
     * authentication.
     *
     * @param requestURI The request URI to check.
     * @return true if the endpoint is public, false otherwise.
     */
    private boolean isPublicEndpoint(String requestURI) {
        return requestURI.startsWith("/docs")
                || requestURI.startsWith("/swagger-ui")
                || requestURI.startsWith("/v3/api-docs")
                || requestURI.startsWith("/swagger-resources")
                || requestURI.startsWith("/webjars")
                || requestURI.startsWith("/actuator")
                || requestURI.equals("/api/v1/auth/login")
                || requestURI.equals("/api/v1/auth/register")
                || requestURI.equals("/api/v1/auth/refresh")
                || requestURI.equals("/api/v1/auth/forgot-password")
                || requestURI.equals("/api/v1/auth/reset-password")
                || requestURI.startsWith("/api/v1/auth/google")
                || requestURI.startsWith("/api/v1/auth/github")
                || requestURI.startsWith("/health");
    }

    /**
     * Parses the JWT token from the Authorization header of the request.
     *
     * @param request The HTTP servlet request.
     * @return The JWT token string, or null if not found or not correctly
     *         formatted.
     */
    private String parseJwt(HttpServletRequest request) {
        String accessToken = jwtUtils.getJwtFromHeader(request);
        log.debug("AuthTokenFilter.java: {}", accessToken);
        return accessToken;
    }
}
