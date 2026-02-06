package com.foodshop.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers configuration for integration tests.
 *
 * <p>Provides PostgreSQL and Redis containers for testing.
 */
@TestConfiguration
public class TestcontainersConfig {

  /**
   * Creates a PostgreSQL container for testing.
   *
   * @return PostgreSQL container
   */
  @Bean
  public PostgreSQLContainer<?> postgresContainer() {
    return new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
        .withDatabaseName("foodshop_test")
        .withUsername("test")
        .withPassword("test")
        .withReuse(true);
  }

  /**
   * Creates a Redis container for testing.
   *
   * @return Redis container
   */
  @Bean
  public GenericContainer<?> redisContainer() {
    return new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
        .withExposedPorts(6379)
        .withReuse(true);
  }
}
