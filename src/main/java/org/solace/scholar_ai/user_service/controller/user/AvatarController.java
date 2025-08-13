package org.solace.scholar_ai.user_service.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solace.scholar_ai.user_service.dto.response.APIResponse;
import org.solace.scholar_ai.user_service.dto.user.AvatarUploadResponse;
import org.solace.scholar_ai.user_service.model.User;
import org.solace.scholar_ai.user_service.repository.UserRepository;
import org.solace.scholar_ai.user_service.service.user.AvatarService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users/me/avatar")
@Tag(name = "Avatar Management", description = "Avatar upload and management endpoints (Cloudinary)")
@RequiredArgsConstructor
@Slf4j
public class AvatarController {

    private final AvatarService avatarService;
    private final UserRepository userRepository;

    @SecurityRequirement(name = "jwtAuth")
    @Operation(summary = "Upload Avatar", description = "Upload avatar via multipart to Cloudinary and update profile")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Avatar uploaded",
                        content = @Content(schema = @Schema(implementation = AvatarUploadResponse.class))),
                @ApiResponse(responseCode = "400", description = "Validation error"),
                @ApiResponse(responseCode = "401", description = "Unauthorized")
            })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<AvatarUploadResponse>> upload(
            Principal principal, @RequestPart("file") MultipartFile file) {
        try {
            String email = principal.getName();
            User user = userRepository
                    .findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

            byte[] bytes = file.getBytes();
            Map<String, Object> uploadResult = avatarService.uploadToCloudinary(
                    user.getId(), file.getOriginalFilename(), file.getContentType(), bytes);
            String secureUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            avatarService.setAvatarFromCloudinary(user.getId(), publicId, secureUrl);

            return ResponseEntity.ok(APIResponse.success(
                    200, "Avatar uploaded successfully", new AvatarUploadResponse(secureUrl, publicId)));
        } catch (Exception e) {
            log.error("Error uploading avatar: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        }
    }

    @SecurityRequirement(name = "jwtAuth")
    @Operation(summary = "Delete Avatar", description = "Delete the current user's avatar")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Avatar deleted successfully"),
                @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token"),
                @ApiResponse(responseCode = "404", description = "User not found")
            })
    @DeleteMapping
    public ResponseEntity<APIResponse<String>> deleteAvatar(Principal principal) {
        try {
            String email = principal.getName();
            log.info("Delete avatar request for user: {}", email);

            User user = userRepository
                    .findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

            avatarService.deleteCurrentAvatar(user.getId());

            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(APIResponse.success(HttpStatus.NO_CONTENT.value(), "Avatar deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting avatar: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        }
    }
}
