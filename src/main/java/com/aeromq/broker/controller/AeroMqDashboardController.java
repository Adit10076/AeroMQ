package com.aeromq.broker.controller;

import com.aeromq.broker.service.AeroMqDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/aeromq/dashboard")
public class AeroMqDashboardController {

    private final AeroMqDashboardService dashboardService;

    public AeroMqDashboardController(AeroMqDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    @GetMapping("/dlq-recent")
    public ResponseEntity<List<Map<String, Object>>> dlqRecent() {
        return ResponseEntity.ok(dashboardService.getRecentDlqEvents());
    }
}
