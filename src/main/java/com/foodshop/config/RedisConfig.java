package com.foodshop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * Redis configuration for session management.
 *
 * <p>Sessions are stored in Redis with a 30-minute timeout.
 */
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800)
public class RedisConfig {
  // RedisConnectionFactory is auto-configured by Spring Boot
}
