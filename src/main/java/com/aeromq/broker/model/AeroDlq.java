package com.aeromq.broker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "aero_dlq")
public class AeroDlq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String topic;

    @Column(name = "exception_message", nullable = false, columnDefinition = "TEXT")
    private String exceptionMessage;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "original_timestamp", nullable = false)
    private long originalTimestamp;

    @Column(name = "failed_at")
    private Long failedAt;

    protected AeroDlq() {
    }

    public AeroDlq(String topic, String exceptionMessage, String payload, long originalTimestamp) {
        this.topic = topic;
        this.exceptionMessage = exceptionMessage;
        this.payload = payload;
        this.originalTimestamp = originalTimestamp;
        this.failedAt = System.currentTimeMillis();
    }

    public Long getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public String getPayload() {
        return payload;
    }

    public long getOriginalTimestamp() {
        return originalTimestamp;
    }

    public long getFailedAt() {
        return failedAt != null ? failedAt : originalTimestamp;
    }
}
