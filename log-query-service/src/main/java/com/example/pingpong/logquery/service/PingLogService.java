package com.example.pingpong.logquery.service;

import com.example.pingpong.shared.entity.PingLog;
import com.example.pingpong.shared.entity.PingLog.RequestStatus;
import com.example.pingpong.logquery.repository.PingLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class PingLogService {
    private final PingLogRepository pingLogRepository;

    public PingLogService(PingLogRepository pingLogRepository) {
        this.pingLogRepository = pingLogRepository;
    }

    public List<String> getUniqueServiceNames() {
        return pingLogRepository.findDistinctServiceNames();
    }

    public Page<PingLog> findLogs(String serviceName, String status, String startTime, String endTime, int page, int size) {
        Specification<PingLog> spec = Specification.where(null);
        
        if (serviceName != null && !serviceName.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("serviceName"), serviceName));
        }
        
        if (status != null && !status.isEmpty()) {
            try {
                RequestStatus requestStatus = RequestStatus.valueOf(status);
                spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), requestStatus));
            } catch (IllegalArgumentException e) {
                // Skip invalid status
            }
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        if (startTime != null && !startTime.isEmpty()) {
            LocalDateTime start = LocalDateTime.parse(startTime, formatter);
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("requestTime"), start));
        }
        
        if (endTime != null && !endTime.isEmpty()) {
            LocalDateTime end = LocalDateTime.parse(endTime, formatter);
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("requestTime"), end));
        }

        Pageable pageable = PageRequest.of(page, size);
        return pingLogRepository.findAll(spec, pageable);
    }

    public PingLog getLog(String id) {
        try {
            Long logId = Long.parseLong(id);
            Optional<PingLog> log = pingLogRepository.findById(logId);
            return log.orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
