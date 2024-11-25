package com.example.pingpong.shared;

import java.util.UUID;

public class ServiceIdentifier {
    private final String serviceName;
    private final UUID instanceId;

    public ServiceIdentifier(String serviceName) {
        this.serviceName = serviceName;
        this.instanceId = UUID.randomUUID();
    }

    public String getServiceName() {
        return serviceName;
    }

    public UUID getInstanceId() {
        return instanceId;
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", serviceName, instanceId);
    }
}
