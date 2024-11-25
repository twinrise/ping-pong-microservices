package com.example.pingpong.logquery.graphql;

import com.example.pingpong.shared.entity.PingLog;
import com.example.pingpong.logquery.service.LogQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@Controller
public class LogQueryResolver {
    private final LogQueryService logQueryService;

    public LogQueryResolver(LogQueryService logQueryService) {
        this.logQueryService = logQueryService;
    }

    @QueryMapping
    public List<PingLog> logsByServiceName(@Argument String serviceName) {
        return logQueryService.findByServiceName(serviceName);
    }

    @QueryMapping
    public List<PingLog> logsByStatus(@Argument String status) {
        return logQueryService.findByStatus(status);
    }

    @QueryMapping
    public List<PingLog> logsByTimeRange(
            @Argument LocalDateTime startTime,
            @Argument LocalDateTime endTime) {
        return logQueryService.findByTimeRange(startTime, endTime);
    }

    @QueryMapping
    public Map<String, Object> logs(
            @Argument("serviceName") String serviceName,
            @Argument("status") String status,
            @Argument("responseStatus") String responseStatus,
            @Argument("startTime") String startTime,
            @Argument("endTime") String endTime,
            @Argument("page") Integer page,
            @Argument("size") Integer size) {
        // 确保页码和大小有默认值
        int pageNum = page != null ? page : 1;
        int pageSize = size != null ? size : 20;
        
        // 转换时间字符串为LocalDateTime
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        
        // 使用上海时区
        ZoneId shanghaiZone = ZoneId.of("Asia/Shanghai");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        if (startTime != null && !startTime.isEmpty()) {
            try {
                startDateTime = LocalDateTime.parse(startTime, formatter);
                System.out.println("Parsed startTime: " + startDateTime);
            } catch (DateTimeParseException e) {
                System.out.println("Error parsing startTime: " + e.getMessage());
            }
        }
        
        if (endTime != null && !endTime.isEmpty()) {
            try {
                endDateTime = LocalDateTime.parse(endTime, formatter);
                System.out.println("Parsed endTime: " + endDateTime);
            } catch (DateTimeParseException e) {
                System.out.println("Error parsing endTime: " + e.getMessage());
            }
        }
        
        // 调用服务层方法
        Page<PingLog> result = logQueryService.getLogs(
                serviceName,
                status,
                responseStatus,
                startDateTime,
                endDateTime,
                PageRequest.of(Math.max(0, pageNum - 1), pageSize));
        
        return Map.of(
            "content", result.getContent(),
            "totalPages", result.getTotalPages(),
            "totalElements", result.getTotalElements(),
            "size", result.getSize(),
            "number", result.getNumber() + 1  // 调整页码，返回给 GraphQL 时从 1 开始
        );
    }
}
