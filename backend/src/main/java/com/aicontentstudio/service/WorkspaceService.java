package com.aicontentstudio.service;

import com.aicontentstudio.dto.request.InviteMemberRequest;
import com.aicontentstudio.dto.request.WorkspaceCreateRequest;
import com.aicontentstudio.dto.response.UserResponse;
import com.aicontentstudio.dto.response.WorkspaceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Workspace lifecycle and membership management.
 */
public interface WorkspaceService {

    WorkspaceResponse createWorkspace(WorkspaceCreateRequest request, String ownerEmail);

    WorkspaceResponse getWorkspace(Long workspaceId, String userEmail);

    Page<WorkspaceResponse> getUserWorkspaces(String userEmail, Pageable pageable);

    WorkspaceResponse updateWorkspace(Long workspaceId, WorkspaceCreateRequest request, String userEmail);

    void deleteWorkspace(Long workspaceId, String userEmail);

    WorkspaceResponse inviteMember(Long workspaceId, InviteMemberRequest request, String inviterEmail);

    WorkspaceResponse acceptInvitation(Long workspaceId, String userEmail);

    void removeMember(Long workspaceId, Long memberId, String requesterEmail);

    List<UserResponse> getMembers(Long workspaceId, String requesterEmail);
}
