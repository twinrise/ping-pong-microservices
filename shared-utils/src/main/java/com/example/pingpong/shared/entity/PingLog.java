package com.example.pingpong.shared.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "ping_logs")
public class PingLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "instance_id", nullable = false)
    private String instanceId;

    @Column(name = "process_id")
    private String processId;

    @Column(name = "request_time", nullable = false)
    private LocalDateTime requestTime;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "response")
    private String response;

    @Column(name = "response_message")
    private String responseMessage;

    @Column(name = "response_status")
    private String responseStatus;

    public enum RequestStatus {
        已发送,       // 已发送
        未发送      // 未发送
    }

    public PingLog() {
    }

    // 成功响应构造函数
    public static PingLog success(String serviceName, String instanceId, String processId, String response) {
        PingLog log = new PingLog();
        log.serviceName = serviceName;
        log.instanceId = instanceId;
        log.processId = processId;
        log.requestTime = LocalDateTime.now();
        log.status = RequestStatus.已发送;
        log.response = response;
        log.errorMessage = null;
        return log;
    }

    // 错误响应构造函数
    public static PingLog error(String serviceName, String instanceId, String processId, String errorMessage) {
        PingLog log = new PingLog();
        log.serviceName = serviceName;
        log.instanceId = instanceId;
        log.processId = processId;
        log.requestTime = LocalDateTime.now();
        log.status = RequestStatus.未发送;
        log.errorMessage = errorMessage;
        log.response = null;
        return log;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
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

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        if (errorMessage != null && !errorMessage.isEmpty()) {
            this.errorMessage = errorMessage;
            this.response = null;  // 清除响应
        }
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        if (response != null && !response.isEmpty()) {
            this.response = response;
            this.errorMessage = null;  // 清除错误消息
        }
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }

    @Override
    public String toString() {
        return String.format("%s,  %s,%s,%s,%s,%s,%s,%s,%s",
            serviceName,
            instanceId,
            processId,
            requestTime.toString(),
            status,
            errorMessage != null ? errorMessage : "",
            response != null ? response : "",
            responseMessage != null ? responseMessage : "",
            responseStatus != null ? responseStatus : ""
        );
    }
}
