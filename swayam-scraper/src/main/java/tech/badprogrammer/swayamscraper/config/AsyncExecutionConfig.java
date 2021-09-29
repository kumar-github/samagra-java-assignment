package tech.badprogrammer.swayamscraper.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Configuration to yield {@link ThreadPoolTaskExecutor} and {@link ThreadPoolTaskScheduler} bean.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncExecutionConfig {

    public static final String ASYNC_EXECUTOR  = "asyncExecutor";
    public static final String ASYNC_SCHEDULER = "asyncScheduler";

    public static final int CORE_POOL_SIZE_FACTOR = 3;
    public static final int MAX_POOL_SIZE_FACTOR  = 7;
    public static final int QUEUE_CAPACITY        = 10_000;

    @Bean(name = ASYNC_EXECUTOR)
    public ThreadPoolTaskExecutor asyncExecutor() {
        final int numberOfCpus = Runtime.getRuntime()
                                        .availableProcessors();
        final ThreadPoolTaskExecutor result = new ThreadPoolTaskExecutor();
        result.setCorePoolSize(CORE_POOL_SIZE_FACTOR * numberOfCpus);
        result.setMaxPoolSize(MAX_POOL_SIZE_FACTOR * numberOfCpus);
        result.setQueueCapacity(QUEUE_CAPACITY);
        return result;
    }

    @Bean(name = ASYNC_SCHEDULER)
    public ThreadPoolTaskScheduler asyncScheduler() {
        final int numOfCpus = Runtime.getRuntime()
                                     .availableProcessors();
        final ThreadPoolTaskScheduler result = new ThreadPoolTaskScheduler();
        result.setPoolSize(CORE_POOL_SIZE_FACTOR * numOfCpus);
        return result;
    }
}
