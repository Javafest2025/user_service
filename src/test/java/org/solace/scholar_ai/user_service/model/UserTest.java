package org.solace.scholar_ai.user_service.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserTest {

    private User user;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        user = new User();
        user.setId(testId);
        user.setEmail("test@example.com");
        user.setEncryptedPassword("hashedPassword123");
        user.setRole(UserRole.USER);
        user.setEmailConfirmed(false);
        user.setLastLoginAt(Instant.now());
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
    }

    @Test
    void testUserCreation() {
        assertNotNull(user);
        assertEquals(testId, user.getId());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("hashedPassword123", user.getEncryptedPassword());
        assertEquals(UserRole.USER, user.getRole());
        assertFalse(user.isEmailConfirmed());
        assertNotNull(user.getLastLoginAt());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
    }

    @Test
    void testUserEmailValidation() {
        // Test that email can be set and retrieved
        user.setEmail("newemail@test.com");
        assertEquals("newemail@test.com", user.getEmail());
    }

    @Test
    void testUserRoleValidation() {
        // Test different roles
        user.setRole(UserRole.USER);
        assertEquals(UserRole.USER, user.getRole());

        user.setRole(UserRole.ADMIN);
        assertEquals(UserRole.ADMIN, user.getRole());
    }

    @Test
    void testUserEmailConfirmation() {
        // Test email confirmation status
        user.setEmailConfirmed(true);
        assertTrue(user.isEmailConfirmed());

        user.setEmailConfirmed(false);
        assertFalse(user.isEmailConfirmed());
    }

    @Test
    void testUserTimestamps() {
        Instant newTime = Instant.now();
        user.setLastLoginAt(newTime);
        assertEquals(newTime, user.getLastLoginAt());
    }

    @Test
    void testUserWithProfile() {
        UserProfile profile = new UserProfile();
        profile.setFullName("John Doe");
        profile.setUser(user);
        user.setProfile(profile);

        assertNotNull(user.getProfile());
        assertEquals("John Doe", user.getProfile().getFullName());
        assertEquals(user, user.getProfile().getUser());
    }
}
