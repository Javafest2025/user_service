package org.solace.scholar_ai.user_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Swagger API documentation.
 * Sets up grouped documentation for Actuator and Application APIs with security, tags, and reusable metadata.
 */
@Configuration
public class SwaggerConfig {

    private static final String ACTUATOR_GROUP = "Actuator API";
    private static final String APPLICATION_GROUP = "Application API";
    private static final String ACTUATOR_PATH_PATTERN = "/actuator/**";
    private static final String APPLICATION_PATH_PATTERN = "/api/**";
    private static final String API_VERSION = "1.0";

    /**
     * Global reusable OpenAPI metadata (title, contact, license, etc.).
     */
    @Bean
    public Info commonApiInfo() {
        return new Info()
                .title("ScholarAI User-Service API")
                .description(
                        "ScholarAI user-service provides services for authentication authorization, account management, and settings.")
                .version(API_VERSION)
                .termsOfService("https://scholarai.dev/terms")
                .contact(new Contact()
                        .name("ScholarAI Dev Team")
                        .url("https://scholarai.dev")
                        .email("support@scholarai.dev"))
                .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0"));
    }

    /**
     * OpenAPI component and security scheme configuration (e.g., JWT Bearer token).
     */
    @Bean
    public OpenAPI baseOpenAPI(Info commonApiInfo) {
        return new OpenAPI()
                .info(commonApiInfo)
                .components(new Components()
                        .addSecuritySchemes(
                                "bearer-jwt",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }

    /**
     * Grouped OpenAPI bean for Actuator endpoints.
     */
    @Bean
    public GroupedOpenApi actuatorApi(Info commonApiInfo) {
        return GroupedOpenApi.builder()
                .group(ACTUATOR_GROUP)
                .displayName("🛠 Actuator Monitoring APIs for user-service")
                .pathsToMatch(ACTUATOR_PATH_PATTERN)
                .addOpenApiCustomizer(openApi -> {
                    openApi.info(commonApiInfo);
                    openApi.addTagsItem(
                            new Tag().name("Actuator").description("Monitoring, metrics and system health"));
                })
                .build();
    }

    /**
     * Grouped OpenAPI bean for ScholarAI application endpoints.
     */
    @Bean
    public GroupedOpenApi applicationApi(Info commonApiInfo) {
        return GroupedOpenApi.builder()
                .group(APPLICATION_GROUP)
                .displayName("📘 ScholarAI user-service APIs")
                .pathsToMatch(APPLICATION_PATH_PATTERN)
                .addOpenApiCustomizer(openApi -> {
                    openApi.info(commonApiInfo);
                    openApi.addTagsItem(new Tag().name("User").description("User profile and account management"));
                    openApi.addTagsItem(new Tag().name("Auth").description("Authentication and authorization flows"));
                })
                .build();
    }
}
