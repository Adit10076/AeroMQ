package com.aeromq.broker.service.impl;

import com.aeromq.broker.model.AeroDlq;
import com.aeromq.broker.repositories.AeroDlqRepository;
import com.aeromq.broker.service.AeroMqDashboardService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class AeroMqDashboardServiceImpl implements AeroMqDashboardService {

    private final ThreadPoolExecutor executor;
    private final AeroDlqRepository dlqRepository;

    public AeroMqDashboardServiceImpl(
            @Qualifier("aeroExecutor") ThreadPoolExecutor executor,
            AeroDlqRepository dlqRepository) {
        this.executor = executor;
        this.dlqRepository = dlqRepository;
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("eventsInQueue", executor.getQueue().size());
        stats.put("activeThreads", executor.getActiveCount());
        stats.put("poolSize", executor.getMaximumPoolSize());
        stats.put("totalCompleted", executor.getCompletedTaskCount());
        stats.put("dlqCount", dlqRepository.count());
        return stats;
    }

    @Override
    public List<Map<String, Object>> getRecentDlqEvents() {
        return dlqRepository.findTop20ByOrderByFailedAtDesc().stream()
                .map(this::toDlqResponse)
                .toList();
    }

    private Map<String, Object> toDlqResponse(AeroDlq entry) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", entry.getId());
        row.put("topic", entry.getTopic());
        row.put("errorMessage", entry.getExceptionMessage());
        row.put("failedAt", entry.getFailedAt());
        return row;
    }
}
