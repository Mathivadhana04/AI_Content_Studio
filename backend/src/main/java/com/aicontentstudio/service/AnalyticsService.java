package com.aicontentstudio.service;

import com.aicontentstudio.dto.response.AnalyticsSummaryResponse;

/**
 * Dashboard analytics aggregation.
 */
public interface AnalyticsService {

    /**
     * Build an analytics summary scoped to a single user.
     */
    AnalyticsSummaryResponse getSummary(String userEmail);

    /**
     * Build a platform-wide analytics summary (ADMIN only).
     */
    AnalyticsSummaryResponse getAdminSummary();
}
