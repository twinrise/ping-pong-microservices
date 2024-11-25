package com.example.pingpong.logquery.repository;

import com.example.pingpong.shared.entity.PingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PingLogRepository extends JpaRepository<PingLog, Long>, JpaSpecificationExecutor<PingLog> {
    @Query(value = "SELECT DISTINCT service_name FROM ping_logs", nativeQuery = true)
    List<String> findDistinctServiceNames();
}
