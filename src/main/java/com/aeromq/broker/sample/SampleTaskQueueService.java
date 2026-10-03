package com.aeromq.broker.sample;

import com.aeromq.broker.service.AeroMqService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class SampleTaskQueueService {

    private static final String TOPIC_ORDERS = "ORDERS";
    private static final String TOPIC_NOTIFICATIONS = "NOTIFICATIONS";

    private final AeroMqService aeroMqService;
    private final ThreadPoolExecutor executor;

    public SampleTaskQueueService(
            AeroMqService aeroMqService,
            @Qualifier("aeroExecutor") ThreadPoolExecutor executor) {
        this.aeroMqService = aeroMqService;
        this.executor = executor;
    }

    public Map<String, Object> runPriorityDemo() {
        int poolSize = executor.getMaximumPoolSize();
        List<Map<String, Object>> jobs = new ArrayList<>();
        jobs.add(aeroMqService.publish(TOPIC_ORDERS, "blocker-1", 5));
        jobs.add(aeroMqService.publish(TOPIC_ORDERS, "order-low-priority", 5));
        jobs.add(aeroMqService.publish(TOPIC_ORDERS, "order-medium-priority", 3));
        jobs.add(aeroMqService.publish(TOPIC_ORDERS, "order-high-priority", 1));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scenario", "priority");
        result.put("description",
                "Dispatched ORDERS in non-priority order (blocker, low, medium, high). "
                        + "Each event fans out to 3 listeners. All tasks enter the priority queue; "
                        + "Processor should run: high (1) → medium (3) → low (5) → blocker (5).");
        result.put("poolSize", poolSize);
        result.put("hint", "Watch [ORDERS][Processor] Started lines — lower priority number runs first.");
        result.put("jobs", jobs);
        return result;
    }

    public Map<String, Object> runBurstDemo(int count) {
        int safeCount = Math.min(Math.max(count, 1), 50);
        List<Map<String, Object>> jobs = new ArrayList<>();

        for (int i = 1; i <= safeCount; i++) {
            jobs.add(aeroMqService.publish(TOPIC_NOTIFICATIONS, "notification-" + i, 3));
        }
        jobs.add(aeroMqService.publish(TOPIC_NOTIFICATIONS, "blocker-1", 5));
        jobs.add(aeroMqService.publish(TOPIC_NOTIFICATIONS, "notification-100", 3));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scenario", "burst");
        result.put("description",
                "Dispatched " + safeCount + " NOTIFICATIONS events. Each fans out to 3 listeners: Dispatcher, Email, Push.");
        result.put("jobs", jobs);
        return result;
    }

    public Map<String, Object> runDlqDemo() {
        Map<String, Object> job = aeroMqService.sendFail("dlq-sample-event", 3);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scenario", "dlq");
        result.put("description",
                "Dispatched a FAILURES event. Fans out to Logger, Metrics (success) and Processor (throws → DLQ after 3 retries).");
        result.put("job", job);
        return result;
    }

    public Map<String, Object> runFullDemo() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scenario", "full");
        result.put("description", "Runs priority, burst, and DLQ demos in sequence.");
        result.put("priorityDemo", runPriorityDemo());
        result.put("burstDemo", runBurstDemo(5));
        result.put("dlqDemo", runDlqDemo());
        return result;
    }
}
