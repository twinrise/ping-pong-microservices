package com.example.pingpong.shared.message;

import java.io.Serializable;
import java.time.Instant;

public class PongMessage implements Serializable {
    private String instanceId;
    private String message;
    private Instant timestamp;
    private String correlationId;

    public PongMessage() {
    }

    public PongMessage(String instanceId, String message, String correlationId) {
        this.instanceId = instanceId;
        this.message = message;
        this.correlationId = correlationId;
        this.timestamp = Instant.now();
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
}
