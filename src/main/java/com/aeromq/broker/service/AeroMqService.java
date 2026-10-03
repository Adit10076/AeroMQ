package com.aeromq.broker.service;

import java.util.Map;

public interface AeroMqService {

    Map<String, Object> send(String message, int priority);

    Map<String, Object> sendFail(String message, int priority);

    Map<String, Object> publish(String topic, String message, int priority);

    Map<String, Object> replay(Long dlqId);
}
