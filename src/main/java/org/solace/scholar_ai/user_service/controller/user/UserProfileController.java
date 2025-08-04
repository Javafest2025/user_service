package org.solace.scholar_ai.user_service.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solace.scholar_ai.user_service.dto.response.APIResponse;
import org.solace.scholar_ai.user_service.dto.user.UserProfileDTO;
import org.solace.scholar_ai.user_service.dto.user.UserProfileResponseDTO;
import org.solace.scholar_ai.user_service.model.User;
import org.solace.scholar_ai.user_service.repository.UserRepository;
import org.solace.scholar_ai.user_service.service.user.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/profile")
@Tag(name = "User Profile", description = "User profile management endpoints for viewing and updating user information")
@RequiredArgsConstructor
public class UserProfileController {
    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);
    private final UserProfileService userProfileService;
    private final UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @SecurityRequirement(name = "jwtAuth")
    @Operation(
            summary = "Get User Profile",
            description =
                    """
            Retrieve the current user's profile information.

            **Returns:**
            - User profile details including name, bio, avatar, etc.
            - Profile is fetched based on the authenticated user's email
            """)
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Profile fetched successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "User Profile",
                                                        value =
                                                                """
                    {
                      "statusCode": 200,
                      "message": "Profile fetched successfully",
                      "success": true,
                      "data": {
                        "id": "123e4567-e89b-12d3-a456-426614174000",
                        "userId": "123e4567-e89b-12d3-a456-426614174001",
                        "fullName": "John Doe",
                        "bio": "Software Engineer passionate about AI",
                        "avatarUrl": "https://example.com/avatar.jpg",
                        "dateOfBirth": "1990-01-01T00:00:00Z",
                        "affiliation": "Tech Company",
                        "positionTitle": "Senior Engineer",
                        "researchInterests": "AI, Machine Learning",
                        "createdAt": "2024-01-01T00:00:00Z",
                        "updatedAt": "2024-01-01T00:00:00Z"
                      }
                    }
                    """))),
                @ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized - invalid or missing token",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Unauthorized",
                                                        value =
                                                                """
                    {
                      "statusCode": 401,
                      "message": "Unauthorized",
                      "success": false,
                      "data": null
                    }
                    """))),
                @ApiResponse(
                        responseCode = "404",
                        description = "Profile not found",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Profile Not Found",
                                                        value =
                                                                """
                    {
                      "statusCode": 404,
                      "message": "Profile not found for user",
                      "success": false,
                      "data": null
                    }
                    """)))
            })
    @GetMapping
    public ResponseEntity<APIResponse<UserProfileResponseDTO>> getProfile(Principal principal) {
        try {
            String email = principal.getName();
            logger.info("Get profile endpoint hit with email: {}", email);
            UserProfileResponseDTO userProfile = userProfileService.getProfileByEmail(email);
            logger.info("Fetched user profile details: {}", objectMapper.writeValueAsString(userProfile));

            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "Profile fetched successfully", userProfile));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage(), null));
        }
    }

    @SecurityRequirement(name = "jwtAuth")
    @Operation(
            summary = "Update User Profile",
            description =
                    """
            Update the current user's profile information.

            **Allowed fields:**
            - fullName, bio, dateOfBirth, affiliation, positionTitle, researchInterests, etc.

            **Note:** Only the provided fields will be updated, others remain unchanged.
            """)
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Profile updated successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Profile Updated",
                                                        value =
                                                                """
                    {
                      "statusCode": 200,
                      "message": "Profile updated successfully",
                      "success": true,
                      "data": {
                        "id": "123e4567-e89b-12d3-a456-426614174000",
                        "userId": "123e4567-e89b-12d3-a456-426614174001",
                        "fullName": "John Smith",
                        "bio": "Updated bio information",
                        "avatarUrl": "https://example.com/avatar.jpg",
                        "dateOfBirth": "1990-01-01T00:00:00Z",
                        "affiliation": "New Company",
                        "positionTitle": "Lead Engineer",
                        "researchInterests": "AI, ML, Deep Learning",
                        "createdAt": "2024-01-01T00:00:00Z",
                        "updatedAt": "2024-01-02T00:00:00Z"
                      }
                    }
                    """))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid profile data or validation failed",
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
                      "message": "Invalid date format for dateOfBirth",
                      "success": false,
                      "data": null
                    }
                    """))),
                @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token"),
                @ApiResponse(responseCode = "404", description = "User not found")
            })
    @PatchMapping
    public ResponseEntity<APIResponse<UserProfileResponseDTO>> updateProfile(
            Principal principal,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "Profile update data",
                            content =
                                    @Content(
                                            examples =
                                                    @ExampleObject(
                                                            name = "Profile Update Example",
                                                            value =
                                                                    """
                    {
                      "fullName": "John Smith",
                      "bio": "Updated bio information",
                      "dateOfBirth": "1990-01-01T00:00:00Z",
                      "affiliation": "New Company",
                      "positionTitle": "Lead Engineer",
                      "researchInterests": "AI, ML, Deep Learning"
                    }
                    """)))
                    @Valid
                    @RequestBody
                    UserProfileDTO userProfileDTO) {
        try {
            String email = principal.getName();
            logger.info("Update profile endpoint hit with email: {}", email);

            User user = userRepository
                    .findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

            UserProfileResponseDTO updatedProfile = userProfileService.updateProfile(user.getId(), userProfileDTO);
            logger.info("Updated user profile details: {}", objectMapper.writeValueAsString(updatedProfile));

            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "Profile updated successfully", updatedProfile));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        }
    }

    @SecurityRequirement(name = "jwtAuth")
    @Operation(
            summary = "Upload Avatar",
            description =
                    """
            Upload a new avatar image for the current user.

            **File requirements:**
            - Supported formats: JPG, PNG, GIF
            - Maximum size: 5MB
            - Recommended dimensions: 200x200 pixels or larger

            **Note:** This is a placeholder implementation. In production, files are uploaded to cloud storage.
            """)
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Avatar uploaded successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Avatar Upload Success",
                                                        value =
                                                                """
                    {
                      "statusCode": 200,
                      "message": "Avatar uploaded successfully",
                      "success": true,
                      "data": {
                        "avatarUrl": "https://placeholder.com/avatar/123e4567-e89b-12d3-a456-426614174000.jpg"
                      }
                    }
                    """))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid file or upload failed",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Upload Error",
                                                        value =
                                                                """
                    {
                      "statusCode": 400,
                      "message": "File is empty or invalid format",
                      "success": false,
                      "data": null
                    }
                    """))),
                @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token"),
                @ApiResponse(responseCode = "404", description = "User not found")
            })
    @PostMapping("/avatar")
    public ResponseEntity<APIResponse<Map<String, String>>> uploadAvatar(
            Principal principal,
            @Parameter(description = "Avatar image file (JPG, PNG, GIF, max 5MB)", example = "avatar.jpg")
                    @RequestParam("avatar")
                    MultipartFile file)
            throws IOException {
        try {
            String email = principal.getName();
            logger.info("Upload avatar endpoint hit with email: {}", email);

            User user = userRepository
                    .findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

            String avatarUrl = uploadToStorage(file);
            UserProfileResponseDTO updatedProfile = userProfileService.updateAvatarUrl(user.getId(), avatarUrl);

            Map<String, String> response = Map.of("avatarUrl", avatarUrl);

            return ResponseEntity.ok(
                    APIResponse.success(HttpStatus.OK.value(), "Avatar uploaded successfully", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        }
    }

    @SecurityRequirement(name = "jwtAuth")
    @Operation(
            summary = "Delete Avatar",
            description =
                    """
            Remove the current user's avatar image.

            **What happens:**
            1. Removes the avatar URL from the user's profile
            2. The avatar image file is marked for deletion (in production)
            3. Profile is updated to reflect the change
            """)
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Avatar deleted successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Avatar Deleted",
                                                        value =
                                                                """
                    {
                      "statusCode": 200,
                      "message": "Avatar deleted successfully",
                      "success": true,
                      "data": null
                    }
                    """))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Deletion failed",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        name = "Deletion Error",
                                                        value =
                                                                """
                    {
                      "statusCode": 400,
                      "message": "Failed to delete avatar",
                      "success": false,
                      "data": null
                    }
                    """))),
                @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token"),
                @ApiResponse(responseCode = "404", description = "User not found")
            })
    @DeleteMapping("/avatar")
    public ResponseEntity<APIResponse<String>> deleteAvatar(Principal principal) {
        try {
            String email = principal.getName();
            logger.info("Delete avatar endpoint hit with email: {}", email);

            User user = userRepository
                    .findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

            userProfileService.deleteAvatarUrl(user.getId());

            return ResponseEntity.ok(APIResponse.success(HttpStatus.OK.value(), "Avatar deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        }
    }

    private String uploadToStorage(MultipartFile file) {
        // TODO: Implement actual file upload logic
        // This is a placeholder - you would typically upload to a cloud storage service
        // like AWS S3, Google Cloud Storage, or Azure Blob Storage

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // For now, return a placeholder URL
        // In a real implementation, you would:
        // 1. Validate file type and size
        // 2. Upload to cloud storage
        // 3. Return the actual URL
        return "https://placeholder.com/avatar/" + UUID.randomUUID() + ".jpg";
    }
}
