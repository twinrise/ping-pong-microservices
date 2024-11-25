package com.example.pingpong.logservice.consumer;

import com.example.pingpong.shared.entity.PingLog;
import com.example.pingpong.shared.entity.PingLog.RequestStatus;
import com.example.pingpong.shared.repository.PingLogRepository;
import com.example.pingpong.shared.message.PingLogMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RocketMQMessageListener(
    topic = "${rocketmq.consumer.topic}",
    consumerGroup = "${rocketmq.consumer.group}"
)
public class LogMessageConsumer implements RocketMQListener<String> {
    private static final Logger logger = LoggerFactory.getLogger(LogMessageConsumer.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PingLogRepository pingLogRepository;

    private String extractMessageFromResponse(String response) {
        try {
            if (response == null || response.isEmpty()) {
                return null;
            }
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.has("message") ? jsonNode.get("message").asText() : null;
        } catch (Exception e) {
            logger.warn("Failed to extract message from response: {}", response, e);
            return null;
        }
    }

    private String extractStatusFromResponse(String response) {
        try {
            if (response == null || response.isEmpty()) {
                return null;
            }
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.has("status") ? jsonNode.get("status").asText() : null;
        } catch (Exception e) {
            logger.warn("Failed to extract status from response: {}", response, e);
            return null;
        }
    }

    @Override
    public void onMessage(String message) {
        try {
            logger.debug("Received message: {}", message);
            PingLogMessage logMessage = objectMapper.readValue(message, PingLogMessage.class);
            
            PingLog pingLog = new PingLog();
            pingLog.setServiceName(logMessage.getServiceName());
            pingLog.setInstanceId(logMessage.getInstanceId().toString());
            pingLog.setProcessId(logMessage.getProcessId());
            pingLog.setRequestTime(logMessage.getRequestTime());
            pingLog.setStatus("已发送".equals(logMessage.getStatus()) ? RequestStatus.已发送 : RequestStatus.未发送);
            pingLog.setErrorMessage(logMessage.getErrorMessage());
            pingLog.setResponse(logMessage.getResponse());
            
            // 从response中提取message和status
            String responseMessage = extractMessageFromResponse(logMessage.getResponse());
            String responseStatus = extractStatusFromResponse(logMessage.getResponse());
            pingLog.setResponseMessage(responseMessage);
            pingLog.setResponseStatus(responseStatus);

            pingLogRepository.save(pingLog);
            logger.info("Successfully saved ping log for service: {}, instance: {}, process: {}, response message: {}, status: {}", 
                logMessage.getServiceName(), logMessage.getInstanceId(), logMessage.getProcessId(), responseMessage, responseStatus);
        } catch (Exception e) {
            logger.error("Error processing message: {}", message, e);
        }
    }
}
