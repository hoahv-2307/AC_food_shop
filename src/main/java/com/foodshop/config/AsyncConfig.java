package com.foodshop.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuration for asynchronous task execution.
 *
 * <p>Enables @Async annotation support and configures a thread pool for async operations like
 * analytics event processing.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

  /**
   * Creates a thread pool executor for async tasks.
   *
   * <p>Configuration:
   *
   * <ul>
   *   <li>Core pool size: 5 threads
   *   <li>Max pool size: 10 threads
   *   <li>Queue capacity: 100 tasks
   *   <li>Thread name prefix: "analytics-async-"
   * </ul>
   *
   * @return the configured executor
   */
  @Bean(name = "taskExecutor")
  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("analytics-async-");
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(60);
    executor.initialize();
    return executor;
  }
}
