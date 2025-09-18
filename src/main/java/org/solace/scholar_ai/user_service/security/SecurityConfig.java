package org.solace.scholar_ai.user_service.security;

import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration class for the application.
 * This class configures Spring Security settings, including HTTP security,
 * user details service, password encoder, and authentication manager.
 */
@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final AuthEntryPointJwt unauthorizedHandler;
    private final AuthTokenFilter authTokenFilter;

    /**
     * Configures the default security filter chain.
     * This method defines the security rules for HTTP requests, including
     * authorization, session management, exception handling, CSRF protection, and
     * custom filters.
     *
     * @param http The HttpSecurity object to configure.
     * @return The configured SecurityFilterChain.
     * @throws Exception If an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        // Swagger/OpenAPI documentation endpoints
                        .requestMatchers(
                                "/docs",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-resources/**",
                                "/webjars/**")
                        .permitAll()
                        // Actuator endpoints for monitoring
                        .requestMatchers("/actuator/**")
                        .permitAll()
                        // All authentication endpoints (public)
                        .requestMatchers("/api/v1/auth/**")
                        .permitAll()
                        // Internal service-to-service communication endpoints
                        .requestMatchers("/api/v1/notifications/send")
                        .permitAll()
                        // Health check endpoints
                        .requestMatchers("/health", "/health/**")
                        .permitAll()
                        // All other requests require authentication
                        .anyRequest()
                        .authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .headers(
                        headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                                .contentSecurityPolicy(
                                        csp -> csp.policyDirectives(
                                                "default-src 'self'; frame-ancestors 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline';")))
                .csrf(csrf -> csrf.disable())
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configures the UserDetailsService bean.
     * This service is responsible for loading user-specific data.
     * It uses a JdbcUserDetailsManager with the provided DataSource.
     *
     * @param dataSource The DataSource to use for retrieving user details.
     * @return A UserDetailsService instance.
     */
    @Bean
    public UserDetailsService userDetailsService(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }

    /**
     * Configures the PasswordEncoder bean.
     * This encoder is used for encoding and verifying passwords.
     * It uses BCryptPasswordEncoder.
     *
     * @return A PasswordEncoder instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the AuthenticationManager bean.
     * This manager is responsible for authenticating users.
     *
     * @param authConfig The AuthenticationConfiguration to use.
     * @return An AuthenticationManager instance.
     * @throws Exception If an error occurs while retrieving the
     *                   AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
