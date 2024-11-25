package com.example.pingpong.shared.message;

import java.io.Serializable;
import java.time.Instant;

public class PingMessage implements Serializable {
    private String instanceId;
    private String message;
    private Instant timestamp;

    public PingMessage() {
    }

    public PingMessage(String instanceId, String message) {
        this.instanceId = instanceId;
        this.message = message;
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
}
