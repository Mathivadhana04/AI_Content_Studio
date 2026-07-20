package com.aicontentstudio.controller;

import com.aicontentstudio.dto.request.ScheduleRequest;
import com.aicontentstudio.dto.response.MessageResponse;
import com.aicontentstudio.entity.ContentSchedule;
import com.aicontentstudio.service.SchedulerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/scheduler")
@RequiredArgsConstructor
@Tag(name = "Scheduler", description = "Scheduled auto-publication entries")
public class SchedulerController {

    private final SchedulerService schedulerService;

    @PostMapping("/schedule/{blogId}")
    @Operation(summary = "Schedule a blog for publication at a future date/time")
    public ResponseEntity<ContentSchedule> scheduleBlog(
            @PathVariable Long blogId,
            @Valid @RequestBody ScheduleRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(schedulerService.scheduleBlog(blogId, request, userDetails.getUsername()));
    }

    @PutMapping("/reschedule/{blogId}")
    @Operation(summary = "Reschedule a blog's publication time")
    public ResponseEntity<ContentSchedule> reschedule(
            @PathVariable Long blogId,
            @Valid @RequestBody ScheduleRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(schedulerService.reschedule(blogId, request, userDetails.getUsername()));
    }

    @DeleteMapping("/cancel/{blogId}")
    @Operation(summary = "Cancel scheduled publication (reverts to draft)")
    public ResponseEntity<MessageResponse> cancelSchedule(
            @PathVariable Long blogId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(schedulerService.cancelSchedule(blogId, userDetails.getUsername()));
    }

    @GetMapping("/scheduled")
    @Operation(summary = "Get list of all pending scheduled publications")
    public ResponseEntity<Page<ContentSchedule>> getScheduled(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(schedulerService.getScheduled(pageable, userDetails.getUsername()));
    }
}
