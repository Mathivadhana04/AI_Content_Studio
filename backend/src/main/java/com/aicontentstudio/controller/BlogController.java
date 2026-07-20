package com.aicontentstudio.controller;

import com.aicontentstudio.dto.request.BlogGenerateRequest;
import com.aicontentstudio.dto.request.BlogUpdateRequest;
import com.aicontentstudio.dto.response.BlogResponse;
import com.aicontentstudio.dto.response.BlogSummaryResponse;
import com.aicontentstudio.service.BlogService;
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

@RestController
@RequestMapping("/api/v1/blogs")
@RequiredArgsConstructor
@Tag(name = "Blogs", description = "Blog CRUD, AI generation, publishing")
public class BlogController {

    private final BlogService blogService;

    @PostMapping("/generate")
    @Operation(summary = "Generate a full AI blog post")
    public ResponseEntity<BlogResponse> generateBlog(
            @Valid @RequestBody BlogGenerateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(blogService.generateBlog(request, userDetails.getUsername()));
    }

    @PostMapping("/draft")
    @Operation(summary = "Create a manual draft blog")
    public ResponseEntity<BlogResponse> createDraft(
            @Valid @RequestBody BlogUpdateRequest request,
            @RequestParam Long workspaceId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(blogService.createDraft(request, workspaceId, userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get blog by ID")
    public ResponseEntity<BlogResponse> getBlog(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(blogService.getBlogById(id, userDetails.getUsername()));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my blogs (paginated)")
    public ResponseEntity<Page<BlogSummaryResponse>> getMyBlogs(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(blogService.getMyBlogs(userDetails.getUsername(), pageable));
    }

    @GetMapping("/workspace/{workspaceId}")
    @Operation(summary = "Get workspace blogs (paginated)")
    public ResponseEntity<Page<BlogSummaryResponse>> getWorkspaceBlogs(
            @PathVariable Long workspaceId,
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(blogService.getWorkspaceBlogs(workspaceId, userDetails.getUsername(), pageable));
    }

    @GetMapping("/workspace/{workspaceId}/search")
    @Operation(summary = "Search blogs in workspace")
    public ResponseEntity<Page<BlogSummaryResponse>> searchBlogs(
            @PathVariable Long workspaceId,
            @RequestParam String query,
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(blogService.searchBlogs(workspaceId, query, userDetails.getUsername(), pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update blog content or metadata")
    public ResponseEntity<BlogResponse> updateBlog(
            @PathVariable Long id,
            @Valid @RequestBody BlogUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(blogService.updateBlog(id, request, userDetails.getUsername()));
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Immediately publish a blog")
    public ResponseEntity<BlogResponse> publishBlog(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(blogService.publishBlog(id, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a blog")
    public ResponseEntity<Void> deleteBlog(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        blogService.deleteBlog(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/duplicate")
    @Operation(summary = "Duplicate a blog as a new draft")
    public ResponseEntity<BlogResponse> duplicateBlog(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(blogService.duplicateBlog(id, userDetails.getUsername()));
    }

    @PostMapping("/{id}/version")
    @Operation(summary = "Save a version snapshot of the current blog content")
    public ResponseEntity<BlogResponse> saveVersion(
            @PathVariable Long id,
            @RequestParam(required = false) String changeNote,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(blogService.saveBlogVersion(id, changeNote, userDetails.getUsername()));
    }

    @GetMapping("/{id}/versions")
    @Operation(summary = "Get list of saved versions of a blog")
    public ResponseEntity<java.util.List<com.aicontentstudio.dto.response.BlogVersionResponse>> getVersions(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(blogService.getBlogVersions(id, userDetails.getUsername()));
    }
}
