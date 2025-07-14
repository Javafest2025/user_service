package org.solace.scholar_ai.user_service.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserProfileTest {

    private UserProfile profile;
    private User user;
    private UUID profileId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        profileId = UUID.randomUUID();
        userId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");

        profile = new UserProfile();
        profile.setId(profileId);
        profile.setUser(user);
        profile.setFullName("John Doe");
        profile.setAvatarUrl("https://example.com/avatar.jpg");
        profile.setPhoneNumber("+1234567890");
        profile.setDateOfBirth(Instant.parse("1990-01-01T00:00:00Z"));
        profile.setBio("Software Engineer");
        profile.setAffiliation("Tech University");
        profile.setPositionTitle("Senior Developer");
        profile.setResearchInterests("AI, Machine Learning");
        profile.setGoogleScholarUrl("https://scholar.google.com/citations?user=123");
        profile.setPersonalWebsiteUrl("https://johndoe.com");
        profile.setOrcidId("0000-0001-2345-6789");
        profile.setLinkedInUrl("https://linkedin.com/in/johndoe");
        profile.setTwitterUrl("https://twitter.com/johndoe");
        profile.setCreatedAt(Instant.now());
        profile.setUpdatedAt(Instant.now());
    }

    @Test
    void testProfileCreation() {
        assertNotNull(profile);
        assertEquals(profileId, profile.getId());
        assertEquals("John Doe", profile.getFullName());
        assertEquals("https://example.com/avatar.jpg", profile.getAvatarUrl());
        assertEquals("+1234567890", profile.getPhoneNumber());
        assertNotNull(profile.getDateOfBirth());
        assertEquals("Software Engineer", profile.getBio());
        assertEquals("Tech University", profile.getAffiliation());
        assertEquals("Senior Developer", profile.getPositionTitle());
        assertEquals("AI, Machine Learning", profile.getResearchInterests());
        assertEquals("https://scholar.google.com/citations?user=123", profile.getGoogleScholarUrl());
        assertEquals("https://johndoe.com", profile.getPersonalWebsiteUrl());
        assertEquals("0000-0001-2345-6789", profile.getOrcidId());
        assertEquals("https://linkedin.com/in/johndoe", profile.getLinkedInUrl());
        assertEquals("https://twitter.com/johndoe", profile.getTwitterUrl());
        assertNotNull(profile.getCreatedAt());
        assertNotNull(profile.getUpdatedAt());
    }

    @Test
    void testProfileUserRelationship() {
        assertEquals(user, profile.getUser());
        assertEquals(userId, profile.getUser().getId());
        assertEquals("test@example.com", profile.getUser().getEmail());
    }

    @Test
    void testProfileFieldUpdates() {
        // Test updating profile fields
        profile.setFullName("Jane Doe");
        assertEquals("Jane Doe", profile.getFullName());

        profile.setBio("Updated bio");
        assertEquals("Updated bio", profile.getBio());

        profile.setAffiliation("New University");
        assertEquals("New University", profile.getAffiliation());
    }

    @Test
    void testProfileSocialLinks() {
        // Test social media links
        profile.setLinkedInUrl("https://linkedin.com/in/janedoe");
        assertEquals("https://linkedin.com/in/janedoe", profile.getLinkedInUrl());

        profile.setTwitterUrl("https://twitter.com/janedoe");
        assertEquals("https://twitter.com/janedoe", profile.getTwitterUrl());

        profile.setGoogleScholarUrl("https://scholar.google.com/citations?user=456");
        assertEquals("https://scholar.google.com/citations?user=456", profile.getGoogleScholarUrl());
    }

    @Test
    void testProfileAcademicInfo() {
        // Test academic information
        profile.setOrcidId("0000-0002-3456-7890");
        assertEquals("0000-0002-3456-7890", profile.getOrcidId());

        profile.setPositionTitle("Associate Professor");
        assertEquals("Associate Professor", profile.getPositionTitle());

        profile.setResearchInterests("Computer Science, Data Science");
        assertEquals("Computer Science, Data Science", profile.getResearchInterests());
    }

    @Test
    void testProfileTimestamps() {
        Instant newTime = Instant.now();
        profile.setCreatedAt(newTime);
        profile.setUpdatedAt(newTime);

        assertEquals(newTime, profile.getCreatedAt());
        assertEquals(newTime, profile.getUpdatedAt());
    }
}
