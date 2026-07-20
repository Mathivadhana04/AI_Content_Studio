package com.aicontentstudio.controller;

import com.aicontentstudio.dto.request.CommentRequest;
import com.aicontentstudio.entity.ActivityLog;
import com.aicontentstudio.entity.BlogComment;
import com.aicontentstudio.service.CollaborationService;
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
@RequestMapping("/api/v1/blogs/{blogId}")
@RequiredArgsConstructor
@Tag(name = "Collaboration", description = "Blog comments, replies, resolves, and activity log audit trails")
public class CollaborationController {

    private final CollaborationService collaborationService;

    @PostMapping("/comments")
    @Operation(summary = "Add a top-level comment to a blog")
    public ResponseEntity<BlogComment> addComment(
            @PathVariable Long blogId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(collaborationService.addComment(blogId, request, userDetails.getUsername()));
    }

    @PostMapping("/comments/{commentId}/reply")
    @Operation(summary = "Reply to a comment")
    public ResponseEntity<BlogComment> replyToComment(
            @PathVariable Long blogId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(collaborationService.replyToComment(commentId, request, userDetails.getUsername()));
    }

    @GetMapping("/comments")
    @Operation(summary = "Get threaded comments for a blog (paginated)")
    public ResponseEntity<Page<BlogComment>> getComments(
            @PathVariable Long blogId,
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(collaborationService.getComments(blogId, pageable, userDetails.getUsername()));
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "Delete a comment")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long blogId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        collaborationService.deleteComment(commentId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/comments/{commentId}/resolve")
    @Operation(summary = "Mark a comment thread as resolved")
    public ResponseEntity<BlogComment> resolveComment(
            @PathVariable Long blogId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(collaborationService.resolveComment(commentId, userDetails.getUsername()));
    }

    @GetMapping("/activity")
    @Operation(summary = "Get activity log audit history for a blog")
    public ResponseEntity<Page<ActivityLog>> getActivityLog(
            @PathVariable Long blogId,
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(collaborationService.getActivityLog(blogId, pageable, userDetails.getUsername()));
    }
}
