package org.solace.scholar_ai.user_service.dto.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {

    @Size(max = 255, message = "Full name must not exceed 255 characters")
    private String fullName;

    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    @Pattern(regexp = "^(https?://.*)?$", message = "Avatar URL must be a valid HTTP/HTTPS URL")
    private String avatarUrl;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Pattern(
            regexp = "^[+]?[0-9\\s\\-\\(\\)]{0,20}$",
            message = "Phone number must contain only digits, spaces, hyphens, parentheses, and optionally a plus sign")
    private String phoneNumber;

    private Instant dateOfBirth;

    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    private String bio;

    @Size(max = 255, message = "Affiliation must not exceed 255 characters")
    private String affiliation;

    @Size(max = 255, message = "Position title must not exceed 255 characters")
    private String positionTitle;

    @Size(max = 1000, message = "Research interests must not exceed 1000 characters")
    private String researchInterests;

    @Size(max = 500, message = "Google Scholar URL must not exceed 500 characters")
    @Pattern(regexp = "^(https?://.*)?$", message = "Google Scholar URL must be a valid HTTP/HTTPS URL")
    private String googleScholarUrl;

    @Size(max = 500, message = "Personal website URL must not exceed 500 characters")
    @Pattern(regexp = "^(https?://.*)?$", message = "Personal website URL must be a valid HTTP/HTTPS URL")
    private String personalWebsiteUrl;

    @Size(max = 50, message = "ORCID ID must not exceed 50 characters")
    @Pattern(
            regexp = "^[0-9]{4}-[0-9]{4}-[0-9]{4}-[0-9]{4}$",
            message = "ORCID ID must be in format XXXX-XXXX-XXXX-XXXX")
    private String orcidId;

    @Size(max = 500, message = "LinkedIn URL must not exceed 500 characters")
    @Pattern(regexp = "^(https?://.*)?$", message = "LinkedIn URL must be a valid HTTP/HTTPS URL")
    private String linkedInUrl;

    @Size(max = 500, message = "Twitter URL must not exceed 500 characters")
    @Pattern(regexp = "^(https?://.*)?$", message = "Twitter URL must be a valid HTTP/HTTPS URL")
    private String twitterUrl;
}
