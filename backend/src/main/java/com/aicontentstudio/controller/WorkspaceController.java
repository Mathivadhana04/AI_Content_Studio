package com.aicontentstudio.controller;

import com.aicontentstudio.dto.request.InviteMemberRequest;
import com.aicontentstudio.dto.request.WorkspaceCreateRequest;
import com.aicontentstudio.dto.response.UserResponse;
import com.aicontentstudio.dto.response.WorkspaceResponse;
import com.aicontentstudio.service.WorkspaceService;
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
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
@Tag(name = "Workspaces", description = "Workspace CRUD, member invitations")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    @Operation(summary = "Create a new workspace")
    public ResponseEntity<WorkspaceResponse> createWorkspace(
            @Valid @RequestBody WorkspaceCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workspaceService.createWorkspace(request, userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get workspace details by ID")
    public ResponseEntity<WorkspaceResponse> getWorkspace(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(workspaceService.getWorkspace(id, userDetails.getUsername()));
    }

    @GetMapping
    @Operation(summary = "Get all workspaces accessible by current user (paginated)")
    public ResponseEntity<Page<WorkspaceResponse>> getUserWorkspaces(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(workspaceService.getUserWorkspaces(userDetails.getUsername(), pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update workspace details")
    public ResponseEntity<WorkspaceResponse> updateWorkspace(
            @PathVariable Long id,
            @Valid @RequestBody WorkspaceCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(workspaceService.updateWorkspace(id, request, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a workspace")
    public ResponseEntity<Void> deleteWorkspace(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        workspaceService.deleteWorkspace(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/members/invite")
    @Operation(summary = "Invite a member to a workspace")
    public ResponseEntity<WorkspaceResponse> inviteMember(
            @PathVariable Long id,
            @Valid @RequestBody InviteMemberRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(workspaceService.inviteMember(id, request, userDetails.getUsername()));
    }

    @PostMapping("/{id}/members/accept")
    @Operation(summary = "Accept an invitation to join a workspace")
    public ResponseEntity<WorkspaceResponse> acceptInvitation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(workspaceService.acceptInvitation(id, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @Operation(summary = "Remove a member from a workspace")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        workspaceService.removeMember(id, userId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "Get all members of a workspace")
    public ResponseEntity<List<UserResponse>> getMembers(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(workspaceService.getMembers(id, userDetails.getUsername()));
    }
}
