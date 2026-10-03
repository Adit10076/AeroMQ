package com.aeromq.broker.service.impl;

import com.aeromq.broker.core.AeroDispatcher;
import com.aeromq.broker.model.AeroDlq;
import com.aeromq.broker.model.AeroEvent;
import com.aeromq.broker.repositories.AeroDlqRepository;
import com.aeromq.broker.service.AeroMqService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AeroMqServiceImpl implements AeroMqService {

    private static final String TOPIC_TRANSACTIONS = "TRANSACTIONS";
    private static final String TOPIC_FAILURES = "FAILURES";
    private static final int DEFAULT_REPLAY_PRIORITY = 3;

    private final AeroDispatcher dispatcher;
    private final AeroDlqRepository dlqRepository;
    private final ObjectMapper objectMapper;

    public AeroMqServiceImpl(
            AeroDispatcher dispatcher,
            AeroDlqRepository dlqRepository,
            ObjectMapper objectMapper) {
        this.dispatcher = dispatcher;
        this.dlqRepository = dlqRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, Object> send(String message, int priority) {
        return publish(TOPIC_TRANSACTIONS, message, priority);
    }

    @Override
    public Map<String, Object> sendFail(String message, int priority) {
        return publish(TOPIC_FAILURES, message, priority);
    }

    @Override
    public Map<String, Object> publish(String topic, String message, int priority) {
        AeroEvent<String> event = new AeroEvent<>(topic, message, priority);
        dispatcher.publish(event);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("topic", topic);
        response.put("message", message);
        response.put("priority", priority);
        response.put("status", "dispatched");
        if (TOPIC_FAILURES.equals(topic)) {
            response.put("note", "Worker will fail; after 3 retries the event is saved to aero_dlq");
        }
        return response;
    }

    @Override
    public Map<String, Object> replay(Long dlqId) {
        AeroDlq dlqEntry = dlqRepository.findById(dlqId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "DLQ entry not found: " + dlqId));

        Object payload = deserializePayload(dlqEntry.getPayload());
        AeroEvent<Object> event = new AeroEvent<>(dlqEntry.getTopic(), payload, DEFAULT_REPLAY_PRIORITY);
        dispatcher.publish(event);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("dlqId", dlqId);
        response.put("topic", dlqEntry.getTopic());
        response.put("originalTimestamp", dlqEntry.getOriginalTimestamp());
        response.put("status", "replayed");
        return response;
    }

    private Object deserializePayload(String payloadJson) {
        if ("null".equals(payloadJson)) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(payloadJson);
            if (node.isTextual()) {
                return node.textValue();
            }
            if (node.isNull()) {
                return null;
            }
            return objectMapper.readValue(payloadJson, Object.class);
        } catch (JsonProcessingException e) {
            return payloadJson;
        }
    }
}
