package com.aeromq.broker.core;

import com.aeromq.broker.model.AeroDlq;
import com.aeromq.broker.model.AeroEvent;
import com.aeromq.broker.repositories.AeroDlqRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

@Component
public class AeroDispatcher {

    private static final Logger log = LoggerFactory.getLogger(AeroDispatcher.class);
    private static final int MAX_RETRIES = 3;

    private final ListenerRegistry registry;
    private final ExecutorService executor;
    private final AeroDlqRepository dlqRepository;
    private final ObjectMapper objectMapper;

    public AeroDispatcher(
            ListenerRegistry registry,
            @Qualifier("aeroExecutor") ExecutorService executor,
            AeroDlqRepository dlqRepository,
            ObjectMapper objectMapper) {
        this.registry = registry;
        this.executor = executor;
        this.dlqRepository = dlqRepository;
        this.objectMapper = objectMapper;
    }

    public void publish(AeroEvent<?> event) {
        List<ListenerRegistry.ConsumerWrapper> consumers = registry.getConsumers(event.getTopic());

        for (ListenerRegistry.ConsumerWrapper consumer : consumers) {
            Runnable listenerTask = () -> {
                try {
                    consumer.method().invoke(consumer.bean(), event.getPayload());
                } catch (Exception e) {
                    throw new RuntimeException("Execution failed", e);
                }
            };

            var priorityTask = new com.aeromq.broker.config.AeroMQConfig.PriorityRunnable(
                    event.getPriority(),
                    () -> {
                        try {
                            listenerTask.run();
                        } catch (Throwable ex) {
                            handleFailure(event, ex);
                        }
                    });

            executor.execute(priorityTask);
        }
    }

    private void handleFailure(AeroEvent<?> event, Throwable failure) {
        if (event.getRetryCount() < MAX_RETRIES) {
            event.incrementRetry();
            log.warn("Retrying event on topic: {} | Attempt: {}", event.getTopic(), event.getRetryCount());
            publish(event);
            return;
        }

        String exceptionMessage = resolveExceptionMessage(failure);
        String payload = serializePayload(event.getPayload());

        AeroDlq dlqEntry = new AeroDlq(
                event.getTopic(),
                exceptionMessage,
                payload,
                event.getTimestamp());

        dlqRepository.save(dlqEntry);
        log.error(
                "Event on topic [{}] permanently failed after {} retries. Saved to aero_dlq (id={}).",
                event.getTopic(),
                MAX_RETRIES,
                dlqEntry.getId());
    }

    private String resolveExceptionMessage(Throwable failure) {
        Throwable root = failure;
        while (root.getCause() != null && (root instanceof CompletionException || root.getMessage() == null)) {
            root = root.getCause();
        }
        return root.getMessage() != null ? root.getMessage() : root.getClass().getName();
    }

    private String serializePayload(Object payload) {
        if (payload == null) {
            return "null";
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return payload.toString();
        }
    }
}
