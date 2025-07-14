package org.solace.scholar_ai.user_service.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserIdentityProviderTest {

    private UserIdentityProvider identityProvider;
    private User user;
    private UUID providerId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        providerId = UUID.randomUUID();
        userId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");

        identityProvider = new UserIdentityProvider();
        identityProvider.setId(providerId);
        identityProvider.setUser(user);
        identityProvider.setProvider("GOOGLE");
        identityProvider.setProviderUserId("google_user_123");
        identityProvider.setCreatedAt(Instant.now());
        identityProvider.setUpdatedAt(Instant.now());
    }

    @Test
    void testIdentityProviderCreation() {
        assertNotNull(identityProvider);
        assertEquals(providerId, identityProvider.getId());
        assertEquals("GOOGLE", identityProvider.getProvider());
        assertEquals("google_user_123", identityProvider.getProviderUserId());
        assertNotNull(identityProvider.getCreatedAt());
        assertNotNull(identityProvider.getUpdatedAt());
    }

    @Test
    void testIdentityProviderUserRelationship() {
        assertEquals(user, identityProvider.getUser());
        assertEquals(userId, identityProvider.getUser().getId());
        assertEquals("test@example.com", identityProvider.getUser().getEmail());
    }

    @Test
    void testDifferentProviders() {
        // Test different OAuth providers
        identityProvider.setProvider("FACEBOOK");
        assertEquals("FACEBOOK", identityProvider.getProvider());

        identityProvider.setProvider("GITHUB");
        assertEquals("GITHUB", identityProvider.getProvider());

        identityProvider.setProvider("LINKEDIN");
        assertEquals("LINKEDIN", identityProvider.getProvider());
    }

    @Test
    void testProviderUserIdUpdates() {
        // Test updating provider user ID
        identityProvider.setProviderUserId("new_google_user_456");
        assertEquals("new_google_user_456", identityProvider.getProviderUserId());

        identityProvider.setProviderUserId("facebook_user_789");
        assertEquals("facebook_user_789", identityProvider.getProviderUserId());
    }

    @Test
    void testTimestamps() {
        Instant newTime = Instant.now();
        identityProvider.setCreatedAt(newTime);
        identityProvider.setUpdatedAt(newTime);

        assertEquals(newTime, identityProvider.getCreatedAt());
        assertEquals(newTime, identityProvider.getUpdatedAt());
    }

    @Test
    void testMultipleProvidersForSameUser() {
        // Test that a user can have multiple identity providers
        UserIdentityProvider facebookProvider = new UserIdentityProvider();
        facebookProvider.setUser(user);
        facebookProvider.setProvider("FACEBOOK");
        facebookProvider.setProviderUserId("facebook_user_123");

        assertEquals(user, facebookProvider.getUser());
        assertEquals("FACEBOOK", facebookProvider.getProvider());
        assertEquals("facebook_user_123", facebookProvider.getProviderUserId());
    }
}
