package com.aicontentstudio.controller;

import com.aicontentstudio.dto.request.SocialPostRequest;
import com.aicontentstudio.entity.SocialPost;
import com.aicontentstudio.service.SocialMediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/social")
@RequiredArgsConstructor
@Tag(name = "Social Media Content", description = "AI social post generators (LinkedIn, Twitter, Facebook, Instagram)")
public class SocialMediaController {

    private final SocialMediaService socialMediaService;

    @PostMapping("/generate")
    @Operation(summary = "Generate a social post based on blog content")
    public ResponseEntity<com.aicontentstudio.dto.response.SocialPostResponse> generatePost(
            @Valid @RequestBody SocialPostRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(com.aicontentstudio.dto.response.SocialPostResponse.fromEntity(
                        socialMediaService.generatePost(request, userDetails.getUsername())));
    }

    @GetMapping("/my")
    @Operation(summary = "Get user's generated social posts (paginated)")
    public ResponseEntity<Page<com.aicontentstudio.dto.response.SocialPostResponse>> getUserPosts(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(socialMediaService.getUserPosts(userDetails.getUsername(), pageable)
                .map(com.aicontentstudio.dto.response.SocialPostResponse::fromEntity));
    }

    @GetMapping("/blog/{blogId}")
    @Operation(summary = "Get all social posts generated from a specific blog")
    public ResponseEntity<List<com.aicontentstudio.dto.response.SocialPostResponse>> getBlogPosts(
            @PathVariable Long blogId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(socialMediaService.getBlogPosts(blogId, userDetails.getUsername()).stream()
                .map(com.aicontentstudio.dto.response.SocialPostResponse::fromEntity)
                .collect(java.util.stream.Collectors.toList()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a social post")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        socialMediaService.deletePost(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
