package com.example.pingpong.shared.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.UUID;

public class PingLogMessage {
    private String serviceName;        // ping服务名
    private UUID instanceId;           // ping服务实例ID，使用UUID类型
    private String processId;          // 进程ID
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestTime; // 请求发送时间
    
    private String status;            // 请求发送状态（已发送/未发送）
    private String message;           // 消息内容
    private String errorMessage;      // 错误消息
    private String response;          // pong响应

    // 默认构造函数
    public PingLogMessage() {
    }

    // 成功响应构造函数
    public static PingLogMessage success(String serviceName, UUID instanceId, String response) {
        PingLogMessage message = new PingLogMessage();
        message.serviceName = serviceName;
        message.instanceId = instanceId;
        message.requestTime = LocalDateTime.now();
        message.status = "已发送";
        message.response = response;
        return message;
    }

    // 错误响应构造函数
    public static PingLogMessage error(String serviceName, UUID instanceId, String errorMessage) {
        PingLogMessage message = new PingLogMessage();
        message.serviceName = serviceName;
        message.instanceId = instanceId;
        message.requestTime = LocalDateTime.now();
        message.status = "未发送";
        message.errorMessage = errorMessage;
        return message;
    }

    // Getters and Setters
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public UUID getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(UUID instanceId) {
        this.instanceId = instanceId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public LocalDateTime getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
