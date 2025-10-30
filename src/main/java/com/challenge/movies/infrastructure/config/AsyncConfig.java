package com.challenge.movies.infrastructure.config;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

  @Value("${movies.async.core-pool-size:5}")
  private int corePoolSize;

  @Value("${movies.async.max-pool-size:10}")
  private int maxPoolSize;

  @Value("${movies.async.queue-capacity:50}")
  private int queueCapacity;

  @Value("${movies.async.thread-name-prefix:MoviesAsync-}")
  private String threadNamePrefix;

  @Bean(name = "moviesTaskExecutor")
  public Executor moviesTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(corePoolSize);
    executor.setMaxPoolSize(maxPoolSize);
    executor.setQueueCapacity(queueCapacity);
    executor.setThreadNamePrefix(threadNamePrefix);
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(60);
    executor.initialize();

    log.info(
        "Initialized async executor - corePoolSize: {}, maxPoolSize: {}, queueCapacity: {}",
        corePoolSize,
        maxPoolSize,
        queueCapacity);

    return executor;
  }
}
