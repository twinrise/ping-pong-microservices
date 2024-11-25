package com.example.pingpong.logquery.resolver;

import com.example.pingpong.shared.entity.PingLog;
import com.example.pingpong.logquery.service.PingLogService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class PingLogResolver {
    private final PingLogService pingLogService;

    public PingLogResolver(PingLogService pingLogService) {
        this.pingLogService = pingLogService;
    }

    @QueryMapping
    public List<String> uniqueServiceNames() {
        return pingLogService.getUniqueServiceNames();
    }

    @QueryMapping
    public PingLog log(@Argument String id) {
        return pingLogService.getLog(id);
    }
}
