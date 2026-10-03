package com.aeromq.broker.sample.listener;

import com.aeromq.broker.core.AeroListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FailureListeners {

    private static final Logger log = LoggerFactory.getLogger(FailureListeners.class);

    @AeroListener(topic = "FAILURES")
    public void processFailure(String data) {
        throw new RuntimeException("[FAILURES][Processor] Simulated failure for DLQ testing: " + data);
    }

    @AeroListener(topic = "FAILURES")
    public void logFailureAttempt(String data) {
        log.warn("[FAILURES][Logger] Observed failing event: {}", data);
    }

    @AeroListener(topic = "FAILURES")
    public void recordFailureMetric(String data) {
        log.info("[FAILURES][Metrics] Incremented failure counter for: {}", data);
    }
}
