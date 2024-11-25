package com.example.pingpong.pong.model;

import java.time.LocalDateTime;

public class PongResponse {
    private String message;
    private String pongInstanceId;
    private LocalDateTime responseTime;
    private String status;

    private PongResponse(String message, String pongInstanceId, 
                        LocalDateTime responseTime, String status) {
        this.message = message;
        this.pongInstanceId = pongInstanceId;
        this.responseTime = responseTime;
        this.status = status;
    }

    // Getters
    public String getMessage() {
        return message;
    }

    public String getPongInstanceId() {
        return pongInstanceId;
    }

    public LocalDateTime getResponseTime() {
        return responseTime;
    }

    public String getStatus() {
        return status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String message;
        private String pongInstanceId;
        private LocalDateTime responseTime;
        private String status;

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder pongInstanceId(String pongInstanceId) {
            this.pongInstanceId = pongInstanceId;
            return this;
        }

        public Builder responseTime(LocalDateTime responseTime) {
            this.responseTime = responseTime;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public PongResponse build() {
            return new PongResponse(message, pongInstanceId, responseTime, status);
        }
    }
}
