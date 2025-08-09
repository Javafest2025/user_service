package org.solace.scholar_ai.user_service.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solace.scholar_ai.user_service.dto.response.APIResponse;
import org.solace.scholar_ai.user_service.dto.user.CommitAvatarRequest;
import org.solace.scholar_ai.user_service.dto.user.UploadUrlResponse;
import org.solace.scholar_ai.user_service.model.User;
import org.solace.scholar_ai.user_service.repository.UserRepository;
import org.solace.scholar_ai.user_service.service.user.AvatarService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me/avatar")
@Tag(name = "Avatar Management", description = "Avatar upload and management endpoints")
@RequiredArgsConstructor
@Slf4j
public class AvatarController {

    private final AvatarService avatarService;
    private final UserRepository userRepository;

    @SecurityRequirement(name = "jwtAuth")
    @Operation(
            summary = "Get Upload URL",
            description = "Get a presigned URL for uploading an avatar image directly to Backblaze B2")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Upload URL generated successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Upload URL Response",
                                                        value =
                                                                """
                    {
                      "statusCode": 200,
                      "message": "Upload URL generated successfully",
                      "success": true,
                      "data": {
                        "putUrl": "https://s3.ams5.backblazeb2.com/scholarai-papers/avatars/123e4567-e89b-12d3-a456-426614174000/abc123.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=...",
                        "key": "avatars/123e4567-e89b-12d3-a456-426614174000/abc123.jpg",
                        "publicUrl": "https://scholarai-papers.s3.ams5.backblazeb2.com/avatars/123e4567-e89b-12d3-a456-426614174000/abc123.jpg"
                      }
                    }
                    """))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid content type or file size",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Validation Error",
                                                        value =
                                                                """
                    {
                      "statusCode": 400,
                      "message": "Invalid content type. Allowed types: [image/png, image/jpeg, image/webp]",
                      "success": false,
                      "data": null
                    }
                    """))),
                @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token"),
                @ApiResponse(responseCode = "404", description = "User not found")
            })
    @PostMapping("/upload-url")
    public ResponseEntity<APIResponse<UploadUrlResponse>> getUploadUrl(
            Principal principal,
            @Parameter(description = "Content type of the image (e.g., image/png, image/jpeg, image/webp)")
                    @RequestParam("contentType")
                    String contentType,
            @Parameter(description = "File size in bytes (max 5MB)") @RequestParam("contentLength")
                    long contentLength) {
        try {
            String email = principal.getName();
            log.info("Get upload URL request for user: {}", email);

            User user = userRepository
                    .findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

            UploadUrlResponse response =
                    avatarService.createPresignedUploadUrl(user.getId(), contentType, contentLength);

            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "Upload URL generated successfully", response));
        } catch (Exception e) {
            log.error("Error generating upload URL: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        }
    }

    @SecurityRequirement(name = "jwtAuth")
    @Operation(
            summary = "Commit Avatar",
            description = "Commit an uploaded avatar by providing the key and ETag from the upload")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Avatar committed successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Avatar Committed",
                                                        value =
                                                                """
                    {
                      "statusCode": 200,
                      "message": "Avatar committed successfully",
                      "success": true,
                      "data": null
                    }
                    """))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid key or ETag",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Validation Error",
                                                        value =
                                                                """
                    {
                      "statusCode": 400,
                      "message": "Invalid avatar key for user",
                      "success": false,
                      "data": null
                    }
                    """))),
                @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token"),
                @ApiResponse(responseCode = "404", description = "User not found")
            })
    @PatchMapping("/commit")
    public ResponseEntity<APIResponse<String>> commitAvatar(
            Principal principal, @Valid @RequestBody CommitAvatarRequest request) {
        try {
            String email = principal.getName();
            log.info("Commit avatar request for user: {}", email);

            User user = userRepository
                    .findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

            avatarService.commitAvatar(user.getId(), request);

            return ResponseEntity.ok(APIResponse.success(HttpStatus.OK.value(), "Avatar committed successfully", null));
        } catch (Exception e) {
            log.error("Error committing avatar: {}", e.getMessage(), e);
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
