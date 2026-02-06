package com.foodshop.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Email configuration for sending notifications.
 *
 * <p>Configures async email sending with thread pool executor.
 */
@Configuration
@EnableAsync
public class MailConfig {

  /**
   * Creates a thread pool executor for async email sending.
   *
   * @return configured executor
   */
  @Bean(name = "emailExecutor")
  public Executor emailExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(5);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("email-");
    executor.initialize();
    return executor;
  }
}
