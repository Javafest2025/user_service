package org.solace.scholar_ai.user_service.service.user;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solace.scholar_ai.user_service.model.UserProfile;
import org.solace.scholar_ai.user_service.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvatarService {

    private final Cloudinary cloudinary;
    private final UserProfileRepository userProfileRepository;

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList("image/png", "image/jpeg", "image/webp");
    private static final long MAX_FILE_SIZE = 3L * 1024L * 1024L; // 3MB

    @Transactional
    public Map<String, Object> uploadToCloudinary(
            UUID userId, String originalFilename, String contentType, byte[] bytes) throws IOException {
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Invalid content type. Allowed types: " + ALLOWED_CONTENT_TYPES);
        }
        if (bytes.length > MAX_FILE_SIZE) {
            double sizeInMB = bytes.length / (1024.0 * 1024.0);
            throw new IllegalArgumentException(
                    String.format("File size (%.1fMB) exceeds maximum limit of 3MB", sizeInMB));
        }

        String folder = "avatars/" + userId;
        String publicId = folder + "/" + UUID.randomUUID();

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) cloudinary
                .uploader()
                .upload(
                        bytes,
                        ObjectUtils.asMap(
                                "resource_type",
                                "image",
                                "public_id",
                                publicId,
                                "overwrite",
                                true,
                                "invalidate",
                                true,
                                "folder",
                                folder,
                                "use_filename",
                                false));
        return result;
    }

    @Transactional
    public void setAvatarFromCloudinary(UUID userId, String publicId, String secureUrl) {
        UserProfile profile = userProfileRepository.findByUserId(userId);
        if (profile == null) {
            throw new IllegalArgumentException("User profile not found");
        }

        String oldPublicId = profile.getAvatarKey();

        profile.setAvatarKey(publicId);
        profile.setAvatarUrl(secureUrl);
        profile.setAvatarEtag(null);
        profile.setAvatarUpdatedAt(Instant.now());
        profile.setUpdatedAt(Instant.now());

        userProfileRepository.save(profile);

        if (oldPublicId != null && !oldPublicId.equals(publicId)) {
            try {
                cloudinary.uploader().destroy(oldPublicId, ObjectUtils.emptyMap());
            } catch (Exception e) {
                // best effort
                log.warn("Failed to delete previous avatar {} from Cloudinary", oldPublicId, e);
            }
        }
    }

    @Transactional
    public void deleteCurrentAvatar(UUID userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId);
        if (profile == null) {
            throw new IllegalArgumentException("User profile not found");
        }

        String publicId = profile.getAvatarKey();
        if (publicId != null && !publicId.isEmpty()) {
            try {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            } catch (Exception e) {
                log.warn("Failed to delete avatar {} from Cloudinary", publicId, e);
            }
        }

        profile.setAvatarKey(null);
        profile.setAvatarUrl(null);
        profile.setAvatarEtag(null);
        profile.setAvatarUpdatedAt(null);
        profile.setUpdatedAt(Instant.now());

        userProfileRepository.save(profile);
    }
}
