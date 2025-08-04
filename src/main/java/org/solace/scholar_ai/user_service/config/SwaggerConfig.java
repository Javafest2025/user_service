package org.solace.scholar_ai.user_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Simple configuration for Swagger API documentation with JWT authentication support.
 * This configuration provides a single unified API documentation with JWT Bearer token authentication.
 */
@Configuration
public class SwaggerConfig {

    /**
     * JWT Bearer security scheme name.
     */
    private static final String JWT_BEARER_SCHEME = "jwtAuth";

    @Value("${server.port:8081}")
    private String serverPort;

    /**
     * Creates the main OpenAPI configuration with security schemes and global settings.
     * This includes JWT Bearer token authentication for the entire API.
     *
     * @return OpenAPI configuration with JWT security scheme
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ScholarAI User Service API")
                        .description(
                                """
                                ## ScholarAI User Service API Documentation

                                This API provides user management and authentication services for ScholarAI platform.

                                ### 🔐 Authentication
                                This API uses JWT Bearer token authentication for protected endpoints. To authenticate:

                                1. **Register** a new account using `/api/v1/auth/register` endpoint
                                2. **Login** using `/api/v1/auth/login` endpoint to get access and refresh tokens
                                3. **Click the 'Authorize' button** above and enter your access token in the format: `Bearer <your-access-token>`
                                4. All protected endpoints will now include the JWT token automatically

                                ### 🔄 Token Management
                                - **Access tokens** expire in 15 minutes
                                - **Refresh tokens** are stored in secure HttpOnly cookies and expire in 7 days
                                - Use `/api/v1/auth/refresh` endpoint to get new access tokens

                                ### 🚀 Quick Start for Developers
                                1. Register or login to get your JWT tokens
                                2. Copy the `accessToken` from the login response
                                3. Click the **🔒 Authorize** button above
                                4. Enter: `Bearer <your-access-token>`
                                5. Now you can test all protected endpoints!

                                ### 📋 Public Endpoints (No Authentication Required)
                                - `POST /api/v1/auth/register` - Register new user
                                - `POST /api/v1/auth/login` - Login user
                                - `POST /api/v1/auth/refresh` - Refresh access token
                                - `POST /api/v1/auth/forgot-password` - Request password reset
                                - `POST /api/v1/auth/reset-password` - Reset password
                                - `GET /actuator/**` - Health and monitoring endpoints

                                ### 🔒 Protected Endpoints (JWT Required)
                                - `POST /api/v1/auth/logout` - Logout user
                                - All other `/api/**` endpoints
                                """)
                        .version("1.0")
                        .contact(
                                new Contact().name("ScholarAI Development Team").email("dev@scholarai.com"))
                        .license(new License().name("MIT License").url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Development Server"),
                        new Server().url("https://api.scholarai.com").description("Production Server")))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        JWT_BEARER_SCHEME,
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                                .name("Authorization")
                                                .description(
                                                        "JWT Bearer token authentication. Enter your token in the format: Bearer <token>")))
                .addSecurityItem(new SecurityRequirement().addList(JWT_BEARER_SCHEME));
    }
}
