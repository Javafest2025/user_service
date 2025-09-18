package org.solace.scholar_ai.user_service.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solace.scholar_ai.user_service.dto.SystemMetricsDto;
import org.solace.scholar_ai.user_service.dto.response.APIResponse;
import org.solace.scholar_ai.user_service.model.UserRole;
import org.solace.scholar_ai.user_service.repository.UserRepository;
import org.solace.scholar_ai.user_service.service.SystemMetricsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Admin endpoints for metrics and management")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final UserRepository userRepository;
    private final SystemMetricsService systemMetricsService;

    @Operation(
            summary = "Get User Count Statistics",
            description = "Returns detailed user count statistics including total users, regular users, and admins")
    @ApiResponse(responseCode = "200", description = "User count statistics retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @GetMapping("/users/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<Map<String, Long>>> getUserCount() {
        try {
            logger.info("Admin user count endpoint hit");
            
            long totalUsers = userRepository.count();
            long regularUsers = userRepository.countByRole(org.solace.scholar_ai.user_service.model.UserRole.USER);
            long adminUsers = userRepository.countByRole(org.solace.scholar_ai.user_service.model.UserRole.ADMIN);

            Map<String, Long> result = Map.of(
                "count", regularUsers,  // Regular users for the main display
                "totalUsers", totalUsers,
                "regularUsers", regularUsers,
                "adminUsers", adminUsers
            );

            logger.info("User count stats - Total: {}, Regular: {}, Admin: {}", totalUsers, regularUsers, adminUsers);

            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "User count statistics retrieved successfully", result));
        } catch (Exception e) {
            logger.error("Error getting user count: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error retrieving user count: " + e.getMessage(),
                            null));
        }
    }

    @Operation(
            summary = "Get System Metrics",
            description = "Returns real-time system performance and resource usage metrics")
    @ApiResponse(responseCode = "200", description = "System metrics retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @GetMapping("/metrics/system")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<SystemMetricsDto>> getSystemMetrics() {
        try {
            logger.info("Admin system metrics endpoint hit");
            SystemMetricsDto metrics = systemMetricsService.getSystemMetrics();

            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "System metrics retrieved successfully", metrics));
        } catch (Exception e) {
            logger.error("Error getting system metrics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error retrieving system metrics: " + e.getMessage(),
                            null));
        }
    }
}
