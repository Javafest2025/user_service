package org.solace.scholar_ai.user_service.controller;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {
    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);
    private final RedisTemplate<String, String> redisTemplate;

    public HealthController(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/redis")
    public ResponseEntity<Map<String, Object>> checkRedisHealth() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Test Redis connection by performing a simple operation
            String testKey = "health_check_test";
            String testValue = "test_value";

            redisTemplate.opsForValue().set(testKey, testValue);
            String retrievedValue = redisTemplate.opsForValue().get(testKey);
            redisTemplate.delete(testKey);

            if (testValue.equals(retrievedValue)) {
                response.put("status", "UP");
                response.put("message", "Redis is healthy and responding");
                response.put("timestamp", System.currentTimeMillis());
                logger.info("Redis health check passed");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "DOWN");
                response.put("message", "Redis is not responding correctly");
                response.put("timestamp", System.currentTimeMillis());
                logger.error("Redis health check failed - value mismatch");
                return ResponseEntity.status(503).body(response);
            }
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("message", "Redis connection failed: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            logger.error("Redis health check failed", e);
            return ResponseEntity.status(503).body(response);
        }
    }
}
