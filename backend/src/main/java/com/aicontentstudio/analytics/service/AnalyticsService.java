// analytics/service/AnalyticsService.java
package com.aicontentstudio.analytics.service;

import com.aicontentstudio.analytics.dto.DashboardMetricsResponse;

public interface AnalyticsService {
    DashboardMetricsResponse getMetrics();
}