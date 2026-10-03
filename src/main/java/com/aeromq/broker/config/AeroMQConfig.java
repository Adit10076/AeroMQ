package com.aeromq.broker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.*;

@Configuration
public class AeroMQConfig {

    @Value("${aeromq.executor.pool-size:0}")
    private int configuredPoolSize;

    @Bean(name = "aeroExecutor")
    public ThreadPoolExecutor aeroExecutor() {
        int poolSize = configuredPoolSize > 0
                ? configuredPoolSize
                : Runtime.getRuntime().availableProcessors();

        return new ThreadPoolExecutor(
                0,
                poolSize,
                60L,
                TimeUnit.SECONDS,
                new PriorityBlockingQueue<>());
    }

    public static class PriorityRunnable implements Runnable, Comparable<PriorityRunnable> {
        private final int priority;
        private final long timestamp;
        private final Runnable targetAction;

        public PriorityRunnable(int priority, Runnable targetAction) {
            this.priority = priority;
            this.timestamp = System.currentTimeMillis();
            this.targetAction = targetAction;
        }

        @Override
        public void run() {
            targetAction.run();
        }

        @Override
        public int compareTo(PriorityRunnable other) {
            int res = Integer.compare(this.priority, other.priority);
            return (res != 0) ? res : Long.compare(this.timestamp, other.timestamp);
        }
    }
}
