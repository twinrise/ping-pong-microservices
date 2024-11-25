package com.example.pingpong.logquery.controller;

import com.example.pingpong.shared.entity.PingLog;
import com.example.pingpong.logquery.service.LogQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogQueryController {
    private final LogQueryService logQueryService;

    public LogQueryController(LogQueryService logQueryService) {
        this.logQueryService = logQueryService;
    }

    @GetMapping("/service/{serviceName}")
    public ResponseEntity<List<PingLog>> findByServiceName(@PathVariable String serviceName) {
        return ResponseEntity.ok(logQueryService.findByServiceName(serviceName));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PingLog>> findByStatus(@PathVariable String status) {
        return ResponseEntity.ok(logQueryService.findByStatus(status));
    }

    @GetMapping("/time-range")
    public ResponseEntity<List<PingLog>> findByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(logQueryService.findByTimeRange(startTime, endTime));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PingLog>> findByConditions(
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(logQueryService.findByConditions(
                serviceName,
                status,
                startTime,
                endTime,
                PageRequest.of(page, size)));
    }
}
