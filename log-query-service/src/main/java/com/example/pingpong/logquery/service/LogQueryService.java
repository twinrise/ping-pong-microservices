package com.example.pingpong.logquery.service;

import com.example.pingpong.shared.entity.PingLog;
import com.example.pingpong.shared.entity.PingLog.RequestStatus;
import com.example.pingpong.logquery.repository.PingLogRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class LogQueryService {
    private final PingLogRepository pingLogRepository;

    public LogQueryService(PingLogRepository pingLogRepository) {
        this.pingLogRepository = pingLogRepository;
    }

    @Cacheable(value = "logs", key = "'service:' + #serviceName", unless = "#serviceName == null || #result.isEmpty()")
    public List<PingLog> findByServiceName(String serviceName) {
        return pingLogRepository.findAll((Specification<PingLog>) (root, query, cb) ->
            cb.equal(root.get("serviceName"), serviceName)
        );
    }

    @Cacheable(value = "logs", key = "'status:' + #status", unless = "#status == null || #result.isEmpty()")
    public List<PingLog> findByStatus(String status) {
        try {
            RequestStatus requestStatus = RequestStatus.valueOf(status);
            return pingLogRepository.findAll((Specification<PingLog>) (root, query, cb) ->
                cb.equal(root.get("status"), requestStatus)
            );
        } catch (IllegalArgumentException e) {
            return List.of(); // Return empty list for invalid status
        }
    }

    @Cacheable(value = "logs", key = "'time:' + #startTime?.toString() + '-' + #endTime?.toString()", 
              unless = "#startTime == null || #endTime == null || #result.isEmpty()")
    public List<PingLog> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return pingLogRepository.findAll((Specification<PingLog>) (root, query, cb) ->
            cb.between(root.get("requestTime"), startTime, endTime)
        );
    }

    @Cacheable(value = "logs", 
              key = "'query:' + (#serviceName != null ? #serviceName : '') + ':' + (#status != null ? #status : '') + ':' + (#startTime != null ? #startTime : '') + ':' + (#endTime != null ? #endTime : '') + ':' + #pageable.pageNumber + ':' + #pageable.pageSize",
              unless = "#result == null || #result.isEmpty()")
    public Page<PingLog> findByConditions(String serviceName, String status, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return pingLogRepository.findAll((Specification<PingLog>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (serviceName != null) {
                predicates.add(cb.equal(root.get("serviceName"), serviceName));
            }
            
            if (status != null) {
                try {
                    RequestStatus requestStatus = RequestStatus.valueOf(status);
                    predicates.add(cb.equal(root.get("status"), requestStatus));
                } catch (IllegalArgumentException e) {
                    // Skip invalid status
                }
            }
            
            if (startTime != null && endTime != null) {
                predicates.add(cb.between(root.get("requestTime"), startTime, endTime));
            }
            
            return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);
    }

    public Page<PingLog> getLogs(String serviceName, String status, String responseStatus, LocalDateTime startTime,
            LocalDateTime endTime, Pageable pageable) {
        System.out.println("Received query parameters:"); // Debug log
        System.out.println("- serviceName: " + serviceName);
        System.out.println("- status: " + status);
        System.out.println("- responseStatus: " + responseStatus);
        System.out.println("- startTime: " + startTime);
        System.out.println("- endTime: " + endTime);

        Specification<PingLog> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (serviceName != null && !serviceName.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("serviceName"), serviceName));
            }

            if (status != null && !status.isEmpty()) {
                try {
                    RequestStatus requestStatus = RequestStatus.valueOf(status);
                    predicates.add(criteriaBuilder.equal(root.get("status"), requestStatus));
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid status value: " + status);
                }
            }

            if (responseStatus != null && !responseStatus.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("responseStatus"), responseStatus));
            }

            if (startTime != null) {
                Predicate startTimePredicate = criteriaBuilder.greaterThanOrEqualTo(root.get("requestTime"), startTime);
                predicates.add(startTimePredicate);
                System.out.println("Added startTime predicate: " + startTime);
            }

            if (endTime != null) {
                Predicate endTimePredicate = criteriaBuilder.lessThanOrEqualTo(root.get("requestTime"), endTime);
                predicates.add(endTimePredicate);
                System.out.println("Added endTime predicate: " + endTime);
            }

            // Always add descending order by requestTime
            query.orderBy(criteriaBuilder.desc(root.get("requestTime")));

            Predicate[] predicateArray = predicates.toArray(new Predicate[0]);
            System.out.println("Number of predicates: " + predicateArray.length);
            return predicates.isEmpty() ? null : criteriaBuilder.and(predicateArray);
        };

        Page<PingLog> result = pingLogRepository.findAll(spec, pageable);
        System.out.println("Query returned " + result.getTotalElements() + " results");
        
        // 打印前几条结果的时间，用于调试
        result.getContent().stream().limit(5).forEach(log -> 
            System.out.println("Result requestTime: " + log.getRequestTime())
        );
        
        return result;
    }
}
