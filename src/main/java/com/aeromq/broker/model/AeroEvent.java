package com.aeromq.broker.model;

public class AeroEvent<T> implements Comparable<AeroEvent<?>> {
    private final String topic;
    private final T payload;
    private final int priority;
    private int retryCount = 0;
    private final long timestamp = System.currentTimeMillis();

    public AeroEvent(String topic, T payload, int priority) {
        this.topic = topic;
        this.payload = payload;
        this.priority = priority;
    }

    public String getTopic() { return topic; }
    public T getPayload() { return payload; }
    public int getPriority() { return priority; }
    public int getRetryCount() { return retryCount; }
    public long getTimestamp() { return timestamp; }
    public void incrementRetry() { this.retryCount++; }

    @Override
    public int compareTo(AeroEvent<?> other) {
        int priorityCompare = Integer.compare(this.priority, other.priority);
        if (priorityCompare != 0) {
            return priorityCompare;
        }
        return Long.compare(this.timestamp, other.timestamp);
    }
}
