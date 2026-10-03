package com.aeromq.broker.service;

import java.util.List;
import java.util.Map;

public interface AeroMqDashboardService {

    Map<String, Object> getStats();

    List<Map<String, Object>> getRecentDlqEvents();
}
