package org.solace.scholar_ai.user_service.service.user;

import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.solace.scholar_ai.user_service.dto.user.UserProfileDTO;
import org.solace.scholar_ai.user_service.dto.user.UserProfileResponseDTO;
import org.solace.scholar_ai.user_service.model.User;
import org.solace.scholar_ai.user_service.model.UserProfile;
import org.solace.scholar_ai.user_service.repository.UserProfileRepository;
import org.solace.scholar_ai.user_service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileResponseDTO getProfileByEmail(String email) {
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        UserProfile userProfile = userProfileRepository.findByUserId(user.getId());

        if (userProfile == null) {
            throw new IllegalArgumentException("User profile not found for user: " + email);
        }

        return convertToResponseDTO(userProfile, user.getId());
    }

    @Transactional
    public UserProfileResponseDTO updateProfile(UUID userId, UserProfileDTO userProfileDTO) {
        UserProfile existingProfile = userProfileRepository.findByUserId(userId);

        if (existingProfile == null) {
            throw new IllegalArgumentException("User profile not found for user ID: " + userId);
        }

        // Update fields if they are not null in the DTO
        if (userProfileDTO.getFullName() != null) {
            existingProfile.setFullName(userProfileDTO.getFullName());
        }
        if (userProfileDTO.getAvatarUrl() != null) {
            existingProfile.setAvatarUrl(userProfileDTO.getAvatarUrl());
        }
        if (userProfileDTO.getPhoneNumber() != null) {
            existingProfile.setPhoneNumber(userProfileDTO.getPhoneNumber());
        }
        if (userProfileDTO.getDateOfBirth() != null) {
            existingProfile.setDateOfBirth(userProfileDTO.getDateOfBirth());
        }
        if (userProfileDTO.getBio() != null) {
            existingProfile.setBio(userProfileDTO.getBio());
        }
        if (userProfileDTO.getAffiliation() != null) {
            existingProfile.setAffiliation(userProfileDTO.getAffiliation());
        }
        if (userProfileDTO.getPositionTitle() != null) {
            existingProfile.setPositionTitle(userProfileDTO.getPositionTitle());
        }
        if (userProfileDTO.getResearchInterests() != null) {
            existingProfile.setResearchInterests(userProfileDTO.getResearchInterests());
        }
        if (userProfileDTO.getGoogleScholarUrl() != null) {
            existingProfile.setGoogleScholarUrl(userProfileDTO.getGoogleScholarUrl());
        }
        if (userProfileDTO.getPersonalWebsiteUrl() != null) {
            existingProfile.setPersonalWebsiteUrl(userProfileDTO.getPersonalWebsiteUrl());
        }
        if (userProfileDTO.getOrcidId() != null) {
            existingProfile.setOrcidId(userProfileDTO.getOrcidId());
        }
        if (userProfileDTO.getLinkedInUrl() != null) {
            existingProfile.setLinkedInUrl(userProfileDTO.getLinkedInUrl());
        }
        if (userProfileDTO.getTwitterUrl() != null) {
            existingProfile.setTwitterUrl(userProfileDTO.getTwitterUrl());
        }

        existingProfile.setUpdatedAt(Instant.now());

        UserProfile savedProfile = userProfileRepository.save(existingProfile);
        return convertToResponseDTO(savedProfile, userId);
    }

    @Transactional
    public UserProfileResponseDTO updateAvatarUrl(UUID userId, String avatarUrl) {
        UserProfile existingProfile = userProfileRepository.findByUserId(userId);

        if (existingProfile == null) {
            throw new IllegalArgumentException("User profile not found for user ID: " + userId);
        }

        existingProfile.setAvatarUrl(avatarUrl);
        existingProfile.setUpdatedAt(Instant.now());

        UserProfile savedProfile = userProfileRepository.save(existingProfile);
        return convertToResponseDTO(savedProfile, userId);
    }

    @Transactional
    public void deleteAvatarUrl(UUID userId) {
        UserProfile existingProfile = userProfileRepository.findByUserId(userId);

        if (existingProfile == null) {
            throw new IllegalArgumentException("User profile not found for user ID: " + userId);
        }

        existingProfile.setAvatarUrl(null);
        existingProfile.setUpdatedAt(Instant.now());

        userProfileRepository.save(existingProfile);
    }

    private UserProfileResponseDTO convertToResponseDTO(UserProfile userProfile, UUID userId) {
        return UserProfileResponseDTO.builder()
                .id(userProfile.getId())
                .userId(userId)
                .fullName(userProfile.getFullName())
                .avatarUrl(userProfile.getAvatarUrl())
                .phoneNumber(userProfile.getPhoneNumber())
                .dateOfBirth(userProfile.getDateOfBirth())
                .bio(userProfile.getBio())
                .affiliation(userProfile.getAffiliation())
                .positionTitle(userProfile.getPositionTitle())
                .researchInterests(userProfile.getResearchInterests())
                .googleScholarUrl(userProfile.getGoogleScholarUrl())
                .personalWebsiteUrl(userProfile.getPersonalWebsiteUrl())
                .orcidId(userProfile.getOrcidId())
                .linkedInUrl(userProfile.getLinkedInUrl())
                .twitterUrl(userProfile.getTwitterUrl())
                .createdAt(userProfile.getCreatedAt())
                .updatedAt(userProfile.getUpdatedAt())
                .build();
    }
}
