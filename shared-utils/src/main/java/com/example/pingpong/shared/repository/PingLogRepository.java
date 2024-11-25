package com.example.pingpong.shared.repository;

import com.example.pingpong.shared.entity.PingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PingLogRepository extends JpaRepository<PingLog, Long> {
}
