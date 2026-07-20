package com.aicontentstudio.controller;

import com.aicontentstudio.entity.HeadlineVariant;
import com.aicontentstudio.dto.response.MessageResponse;
import com.aicontentstudio.service.HeadlineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/headlines")
@RequiredArgsConstructor
@Tag(name = "Headlines", description = "AI headline alternative generation and selection")
public class HeadlineController {

    private final HeadlineService headlineService;

    @PostMapping("/generate/{blogId}")
    @Operation(summary = "Generate 4 headline alternatives (SEO, professional, clickbait, LinkedIn)")
    public ResponseEntity<List<HeadlineVariant>> generateHeadlines(
            @PathVariable Long blogId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(headlineService.generateHeadlines(blogId, userDetails.getUsername()));
    }

    @GetMapping("/{blogId}")
    @Operation(summary = "Get all generated headline variants for a blog")
    public ResponseEntity<List<HeadlineVariant>> getHeadlines(
            @PathVariable Long blogId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(headlineService.getHeadlines(blogId, userDetails.getUsername()));
    }

    @PutMapping("/select/{variantId}")
    @Operation(summary = "Select a headline alternative (renames the blog post)")
    public ResponseEntity<MessageResponse> selectHeadline(
            @PathVariable Long variantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        String newTitle = headlineService.selectHeadline(variantId, userDetails.getUsername());
        return ResponseEntity.ok(MessageResponse.of(newTitle));
    }
}
