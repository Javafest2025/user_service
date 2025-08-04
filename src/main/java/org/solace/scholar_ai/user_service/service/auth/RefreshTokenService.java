package org.solace.scholar_ai.user_service.service.auth;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class RefreshTokenService {
    private static final String REDIS_REFRESH_TOKEN_PREFIX = "refresh_token";
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${spring.app.refresh.expiration-ms}")
    private long refreshTokenValidityMs;

    public RefreshTokenService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveRefreshToken(String username, String refreshToken) {
        Assert.notNull(username, "Username cannot be null");
        Assert.notNull(refreshToken, "Refresh token cannot be null");

        String key = REDIS_REFRESH_TOKEN_PREFIX + username;
        redisTemplate.opsForValue().set(key, refreshToken, Duration.ofMillis(refreshTokenValidityMs));
    }

    public String getRefreshToken(String username) {
        Assert.notNull(username, "Username cannot be null");
        return redisTemplate.opsForValue().get(REDIS_REFRESH_TOKEN_PREFIX + username);
    }

    public void deleteRefreshToken(String username) {
        Assert.notNull(username, "Username cannot be null");
        redisTemplate.delete(REDIS_REFRESH_TOKEN_PREFIX + username);
    }

    public boolean isRefreshTokenValid(String username, String refreshToken) {
        Assert.notNull(username, "Username cannot be null");
        Assert.notNull(refreshToken, "Refresh token cannot be null");

        String storedToken = getRefreshToken(username);
        return storedToken != null && storedToken.equals(refreshToken);
    }
}
