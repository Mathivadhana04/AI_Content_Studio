package com.aicontentstudio.controller;

import com.aicontentstudio.dto.response.AnalyticsSummaryResponse;
import com.aicontentstudio.dto.response.MessageResponse;
import com.aicontentstudio.entity.ActivityLog;
import com.aicontentstudio.repository.ActivityLogRepository;
import com.aicontentstudio.service.AnalyticsService;
import com.aicontentstudio.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin Console", description = "System-wide admin management and auditing logs (ADMIN only)")
public class AdminController {

    private final UserService userService;
    private final AnalyticsService analyticsService;
    private final ActivityLogRepository activityLogRepository;

    @PutMapping("/users/{id}/activate")
    @Operation(summary = "Re-activate a deactivated user account")
    public ResponseEntity<MessageResponse> activateUser(@PathVariable Long id) {
        userService.activateUser(id);
        return ResponseEntity.ok(MessageResponse.of("User account activated successfully"));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get platform metrics dashboard data")
    public ResponseEntity<AnalyticsSummaryResponse> getAdminStats() {
        return ResponseEntity.ok(analyticsService.getAdminSummary());
    }

    @GetMapping("/activity-logs")
    @Operation(summary = "Get platform auditing activity logs (paginated)")
    public ResponseEntity<Page<ActivityLog>> getActivityLogs(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(activityLogRepository.findAllByOrderByCreatedAtDesc(pageable));
    }
}
