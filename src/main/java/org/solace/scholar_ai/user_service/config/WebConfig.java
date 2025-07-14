package org.solace.scholar_ai.user_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Comma-separated origins, resolved from application-*.yml
    @Value("${cors.allowed-origins}")
    private String allowedOriginsRaw;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] allowedOrigins = allowedOriginsRaw.replaceAll("\\s+", "").split(",");

        log.info("Active CORS Origins from property: {}", (Object) allowedOrigins);

        registry.addMapping("/api/**")
                .allowedOriginPatterns(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
