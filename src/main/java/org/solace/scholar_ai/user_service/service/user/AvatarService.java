package org.solace.scholar_ai.user_service.service.user;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solace.scholar_ai.user_service.dto.user.CommitAvatarRequest;
import org.solace.scholar_ai.user_service.dto.user.UploadUrlResponse;
import org.solace.scholar_ai.user_service.model.UserProfile;
import org.solace.scholar_ai.user_service.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvatarService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final UserProfileRepository userProfileRepository;

    @Value("${b2.bucket.name}")
    private String bucketName;

    @Value("${b2.public.cdn.base:https://scholarai-papers.s3.ams5.backblazeb2.com}")
    private String publicCdnBase;

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList("image/png", "image/jpeg", "image/webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Duration PRESIGN_DURATION = Duration.ofMinutes(10);

    @Transactional
    public UploadUrlResponse createPresignedUploadUrl(UUID userId, String contentType, long contentLength) {
        // Validate content type
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Invalid content type. Allowed types: " + ALLOWED_CONTENT_TYPES);
        }

        // Validate file size
        if (contentLength > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 5MB");
        }

        // Generate unique key
        String extension = getExtensionFromContentType(contentType);
        String key = String.format("avatars/%s/%s.%s", userId, UUID.randomUUID(), extension);

        // Create presigned URL
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(PRESIGN_DURATION)
                .putObjectRequest(putObjectRequest)
                .build();

        String presignedUrl = s3Presigner.presignPutObject(presignRequest).url().toString();
        String publicUrl = publicCdnBase + "/" + key;

        return new UploadUrlResponse(presignedUrl, key, publicUrl);
    }

    @Transactional
    public void commitAvatar(UUID userId, CommitAvatarRequest request) {
        // Validate key belongs to user
        if (!request.getKey().startsWith("avatars/" + userId + "/")) {
            throw new IllegalArgumentException("Invalid avatar key for user");
        }

        // Optional: Verify object exists and type
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(request.getKey())
                    .build();
            s3Client.headObject(headRequest);
        } catch (NoSuchKeyException e) {
            throw new IllegalArgumentException("Avatar object not found");
        }

        // Get current profile
        UserProfile profile = userProfileRepository.findByUserId(userId);
        if (profile == null) {
            throw new IllegalArgumentException("User profile not found");
        }

        // Store old key for deletion
        String oldKey = profile.getAvatarKey();

        // Update profile with new avatar info
        profile.setAvatarKey(request.getKey());
        profile.setAvatarUrl(publicCdnBase + "/" + request.getKey());
        profile.setAvatarEtag(request.getEtag());
        profile.setAvatarUpdatedAt(Instant.now());
        profile.setUpdatedAt(Instant.now());

        userProfileRepository.save(profile);

        // Delete old avatar if it exists and is different
        if (oldKey != null && !oldKey.equals(request.getKey())) {
            deleteAvatarFromStorage(oldKey);
        }
    }

    @Transactional
    public void deleteCurrentAvatar(UUID userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId);
        if (profile == null) {
            throw new IllegalArgumentException("User profile not found");
        }

        String currentKey = profile.getAvatarKey();
        if (currentKey != null) {
            // Delete from storage
            deleteAvatarFromStorage(currentKey);
        }

        // Clear avatar fields
        profile.setAvatarKey(null);
        profile.setAvatarUrl(null);
        profile.setAvatarEtag(null);
        profile.setAvatarUpdatedAt(null);
        profile.setUpdatedAt(Instant.now());

        userProfileRepository.save(profile);
    }

    private void deleteAvatarFromStorage(String key) {
        try {
            DeleteObjectRequest deleteRequest =
                    DeleteObjectRequest.builder().bucket(bucketName).key(key).build();
            s3Client.deleteObject(deleteRequest);
            log.info("Deleted avatar from storage: {}", key);
        } catch (Exception e) {
            // Log error but don't fail the operation
            log.warn("Failed to delete avatar from storage: {}", key, e);
        }
    }

    private String getExtensionFromContentType(String contentType) {
        return switch (contentType) {
            case "image/png" -> "png";
            case "image/jpeg" -> "jpg";
            case "image/webp" -> "webp";
            default -> throw new IllegalArgumentException("Unsupported content type: " + contentType);
        };
    }
}
