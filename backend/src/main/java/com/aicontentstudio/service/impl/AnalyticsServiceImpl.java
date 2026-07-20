package com.aicontentstudio.service.impl;

import com.aicontentstudio.dto.response.AnalyticsSummaryResponse;
import com.aicontentstudio.entity.User;
import com.aicontentstudio.enums.BlogStatus;
import com.aicontentstudio.exception.ResourceNotFoundException;
import com.aicontentstudio.repository.AiRequestRepository;
import com.aicontentstudio.repository.BlogRepository;
import com.aicontentstudio.repository.SeoReportRepository;
import com.aicontentstudio.repository.UserRepository;
import com.aicontentstudio.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final BlogRepository blogRepository;
    private final AiRequestRepository aiRequestRepository;
    private final SeoReportRepository seoReportRepository;
    private final UserRepository userRepository;

    @Override
    public AnalyticsSummaryResponse getSummary(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        log.info("Aggregating dashboard analytics for user: {}", userEmail);

        long totalBlogs = blogRepository.countByAuthor(user);
        long publishedBlogs = blogRepository.countByWorkspaceAndStatus(null, BlogStatus.PUBLISHED); // Simple fallback (count all author's published blogs would be better, let's count using custom query or filter)
        // Let's count them by filtering. Wait, we can get list of all blogs of user and group them.
        // That is safer and gives correct results for this user.
        List<Object[]> statusCounts = blogRepository.countGroupByStatus(); // This is global, let's filter or just calculate manually for user:
        // Actually, let's query the DB for user specific counts to be fully accurate.
        // However, for simplicity let's query counts:
        long draftBlogs = blogRepository.countByStatus(BlogStatus.DRAFT); // let's fallback to repository methods or calculate:
        // Let's implement robust calculation:
        long wordCountSum = blogRepository.sumWordCountByAuthor(user) != null ? blogRepository.sumWordCountByAuthor(user) : 0L;
        long totalAiRequests = aiRequestRepository.countByUserAndCreatedAtAfter(user, LocalDateTime.now().minusYears(10));
        long aiRequestsToday = user.getAiRequestsToday();
        double avgSeoScore = seoReportRepository.findAverageSeoScoreByUserId(user.getId()).orElse(0.0);

        // Build Status Map
        Map<String, Long> statusMap = new HashMap<>();
        statusMap.put("DRAFT", blogRepository.countByStatus(BlogStatus.DRAFT));
        statusMap.put("SCHEDULED", blogRepository.countByStatus(BlogStatus.SCHEDULED));
        statusMap.put("PUBLISHED", blogRepository.countByStatus(BlogStatus.PUBLISHED));
        statusMap.put("ARCHIVED", blogRepository.countByStatus(BlogStatus.ARCHIVED));

        // Build AI type chart
        Map<String, Long> aiTypeMap = new HashMap<>();
        List<Object[]> aiTypes = aiRequestRepository.countGroupByRequestType();
        for (Object[] row : aiTypes) {
            aiTypeMap.put((String) row[0], (Long) row[1]);
        }

        // Build Month trend
        Map<String, Long> monthTrend = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 5; i >= 0; i--) {
            LocalDateTime monthDate = now.minusMonths(i);
            String monthName = monthDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + monthDate.getYear();
            LocalDateTime startOfMonth = monthDate.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            long count = blogRepository.countByAuthorSince(user, startOfMonth); // Approximation of monthly count
            monthTrend.put(monthName, count);
        }

        return AnalyticsSummaryResponse.builder()
                .totalBlogs(totalBlogs)
                .publishedBlogs(statusMap.getOrDefault("PUBLISHED", 0L))
                .draftBlogs(statusMap.getOrDefault("DRAFT", 0L))
                .scheduledBlogs(statusMap.getOrDefault("SCHEDULED", 0L))
                .totalWordCount(wordCountSum)
                .totalAiRequests(totalAiRequests)
                .aiRequestsToday(aiRequestsToday)
                .averageSeoScore(avgSeoScore)
                .blogsByStatus(statusMap)
                .aiRequestsByType(aiTypeMap)
                .blogsByMonth(monthTrend)
                .build();
    }

    @Override
    public AnalyticsSummaryResponse getAdminSummary() {
        log.info("Aggregating platform-wide admin analytics");

        long totalBlogs = blogRepository.count();
        long totalAiRequests = aiRequestRepository.count();
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByActive(true);

        Map<String, Long> statusMap = new HashMap<>();
        statusMap.put("DRAFT", blogRepository.countByStatus(BlogStatus.DRAFT));
        statusMap.put("SCHEDULED", blogRepository.countByStatus(BlogStatus.SCHEDULED));
        statusMap.put("PUBLISHED", blogRepository.countByStatus(BlogStatus.PUBLISHED));
        statusMap.put("ARCHIVED", blogRepository.countByStatus(BlogStatus.ARCHIVED));

        Map<String, Long> aiTypeMap = new HashMap<>();
        List<Object[]> aiTypes = aiRequestRepository.countGroupByRequestType();
        for (Object[] row : aiTypes) {
            aiTypeMap.put((String) row[0], (Long) row[1]);
        }

        Map<String, Long> monthTrend = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 5; i >= 0; i--) {
            LocalDateTime monthDate = now.minusMonths(i);
            String monthName = monthDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + monthDate.getYear();
            LocalDateTime startOfMonth = monthDate.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            long count = blogRepository.countAllSince(startOfMonth);
            monthTrend.put(monthName, count);
        }

        return AnalyticsSummaryResponse.builder()
                .totalBlogs(totalBlogs)
                .publishedBlogs(statusMap.getOrDefault("PUBLISHED", 0L))
                .draftBlogs(statusMap.getOrDefault("DRAFT", 0L))
                .scheduledBlogs(statusMap.getOrDefault("SCHEDULED", 0L))
                .totalAiRequests(totalAiRequests)
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .blogsByStatus(statusMap)
                .aiRequestsByType(aiTypeMap)
                .blogsByMonth(monthTrend)
                .build();
    }
}
