package com.aicontentstudio.controller;

import com.aicontentstudio.dto.request.SeoAnalyzeRequest;
import com.aicontentstudio.dto.response.SeoReportResponse;
import com.aicontentstudio.service.SeoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/seo")
@RequiredArgsConstructor
@Tag(name = "SEO Analyzer", description = "Algorithmic SEO scoring and recommendations")
public class SeoController {

    private final SeoService seoService;

    @PostMapping("/analyze")
    @Operation(summary = "Analyze a blog's SEO quality and get score breakdown")
    public ResponseEntity<SeoReportResponse> analyzeBlog(
            @Valid @RequestBody SeoAnalyzeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(seoService.analyzeBlog(
                request.getBlogId(),
                userDetails.getUsername(),
                request.getTargetKeyword()
        ));
    }

    @GetMapping("/report/{blogId}")
    @Operation(summary = "Get the latest SEO report for a specific blog")
    public ResponseEntity<SeoReportResponse> getLatestReport(
            @PathVariable Long blogId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(seoService.getLatestReport(blogId, userDetails.getUsername()));
    }

    @PostMapping("/analyze-text")
    @Operation(summary = "Analyze arbitrary text block SEO quality with custom keywords")
    public ResponseEntity<SeoReportResponse> analyzeText(
            @Valid @RequestBody com.aicontentstudio.dto.request.SeoAnalyzeTextRequest request) {
        return ResponseEntity.ok(seoService.analyzeText(request.getText(), request.getTargetKeyword()));
    }
}
