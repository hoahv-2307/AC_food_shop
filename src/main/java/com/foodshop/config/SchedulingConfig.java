package com.foodshop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration for scheduled tasks.
 *
 * <p>Enables Spring's @Scheduled annotation support for cron jobs and periodic tasks.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
  // No additional configuration needed - @EnableScheduling is sufficient
}
