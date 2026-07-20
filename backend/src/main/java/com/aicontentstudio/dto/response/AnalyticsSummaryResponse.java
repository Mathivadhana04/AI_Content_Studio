package com.aicontentstudio.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AnalyticsSummaryResponse {
    // Blog stats
    private long totalBlogs;
    private long publishedBlogs;
    private long draftBlogs;
    private long scheduledBlogs;
    private long totalWordCount;

    // AI stats
    private long totalAiRequests;
    private long aiRequestsToday;

    // SEO stats
    private double averageSeoScore;

    // User stats
    private long totalUsers;
    private long activeUsers;

    // Charts data
    private Map<String, Long> blogsByStatus;
    private Map<String, Long> aiRequestsByType;
    private Map<String, Long> blogsByMonth;
}
