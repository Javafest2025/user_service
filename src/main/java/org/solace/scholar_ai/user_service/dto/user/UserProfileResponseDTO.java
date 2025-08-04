package org.solace.scholar_ai.user_service.dto.user;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponseDTO {
    private UUID id;
    private UUID userId;
    private String fullName;
    private String avatarUrl;
    private String phoneNumber;
    private Instant dateOfBirth;
    private String bio;
    private String affiliation;
    private String positionTitle;
    private String researchInterests;
    private String googleScholarUrl;
    private String personalWebsiteUrl;
    private String orcidId;
    private String linkedInUrl;
    private String twitterUrl;
    private Instant createdAt;
    private Instant updatedAt;
}
