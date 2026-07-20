package com.aicontentstudio.controller;

import com.aicontentstudio.dto.response.AnalyticsSummaryResponse;
import com.aicontentstudio.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Dashboard analytics for blogs, SEO, and AI usage logs")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    @Operation(summary = "Get user-specific dashboard metrics and charts data")
    public ResponseEntity<AnalyticsSummaryResponse> getSummary(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(analyticsService.getSummary(userDetails.getUsername()));
    }

    @GetMapping("/admin/summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get platform-wide admin metrics and charts data (ADMIN only)")
    public ResponseEntity<AnalyticsSummaryResponse> getAdminSummary() {
        return ResponseEntity.ok(analyticsService.getAdminSummary());
    }
}
