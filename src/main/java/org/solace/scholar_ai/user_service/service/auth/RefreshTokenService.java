package org.solace.scholar_ai.user_service.service.auth;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class RefreshTokenService {
    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);
    private static final String REDIS_REFRESH_TOKEN_PREFIX = "refresh_token";
    private final RedisTemplate<String, String> redisTemplate;

    // Fallback in-memory storage for when Redis is unavailable
    private final ConcurrentHashMap<String, String> fallbackStorage = new ConcurrentHashMap<>();

    @Value("${spring.app.refresh.expiration-ms}")
    private long refreshTokenValidityMs;

    public RefreshTokenService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveRefreshToken(String username, String refreshToken) {
        Assert.notNull(username, "Username cannot be null");
        Assert.notNull(refreshToken, "Refresh token cannot be null");

        try {
            String key = REDIS_REFRESH_TOKEN_PREFIX + ":" + username;
            redisTemplate.opsForValue().set(key, refreshToken, Duration.ofMillis(refreshTokenValidityMs));
            logger.debug("Saved refresh token for user: {} in Redis", username);
        } catch (Exception e) {
            logger.warn("Failed to save refresh token in Redis for user: {}, using fallback storage", username, e);
            // Fallback to in-memory storage
            fallbackStorage.put(username, refreshToken);
        }
    }

    public String getRefreshToken(String username) {
        Assert.notNull(username, "Username cannot be null");

        try {
            String key = REDIS_REFRESH_TOKEN_PREFIX + ":" + username;
            String token = redisTemplate.opsForValue().get(key);
            if (token != null) {
                logger.debug("Retrieved refresh token for user: {} from Redis", username);
                return token;
            }
        } catch (Exception e) {
            logger.warn("Failed to get refresh token from Redis for user: {}, trying fallback storage", username, e);
        }

        // Fallback to in-memory storage
        String fallbackToken = fallbackStorage.get(username);
        if (fallbackToken != null) {
            logger.debug("Retrieved refresh token for user: {} from fallback storage", username);
        }
        return fallbackToken;
    }

    public void deleteRefreshToken(String username) {
        Assert.notNull(username, "Username cannot be null");

        try {
            String key = REDIS_REFRESH_TOKEN_PREFIX + ":" + username;
            redisTemplate.delete(key);
            logger.debug("Deleted refresh token for user: {} from Redis", username);
        } catch (Exception e) {
            logger.warn("Failed to delete refresh token from Redis for user: {}, trying fallback storage", username, e);
        }

        // Also delete from fallback storage
        fallbackStorage.remove(username);
        logger.debug("Deleted refresh token for user: {} from fallback storage", username);
    }

    public boolean isRefreshTokenValid(String username, String refreshToken) {
        Assert.notNull(username, "Username cannot be null");
        Assert.notNull(refreshToken, "Refresh token cannot be null");

        try {
            String storedToken = getRefreshToken(username);
            boolean isValid = storedToken != null && storedToken.equals(refreshToken);
            logger.debug("Refresh token validation for user: {} - valid: {}", username, isValid);
            return isValid;
        } catch (Exception e) {
            logger.error("Failed to validate refresh token for user: {}", username, e);
            return false;
        }
    }
}
