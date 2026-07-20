package com.aicontentstudio.service.impl;

import com.aicontentstudio.dto.request.InviteMemberRequest;
import com.aicontentstudio.dto.request.WorkspaceCreateRequest;
import com.aicontentstudio.dto.response.UserResponse;
import com.aicontentstudio.dto.response.WorkspaceResponse;
import com.aicontentstudio.entity.User;
import com.aicontentstudio.entity.Workspace;
import com.aicontentstudio.entity.WorkspaceMember;
import com.aicontentstudio.enums.NotificationType;
import com.aicontentstudio.enums.WorkspaceRole;
import com.aicontentstudio.exception.BadRequestException;
import com.aicontentstudio.exception.DuplicateResourceException;
import com.aicontentstudio.exception.ResourceNotFoundException;
import com.aicontentstudio.exception.UnauthorizedException;
import com.aicontentstudio.repository.BlogRepository;
import com.aicontentstudio.repository.UserRepository;
import com.aicontentstudio.repository.WorkspaceMemberRepository;
import com.aicontentstudio.repository.WorkspaceRepository;
import com.aicontentstudio.service.NotificationService;
import com.aicontentstudio.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;
    private final BlogRepository blogRepository;
    private final NotificationService notificationService;

    @Override
    public WorkspaceResponse createWorkspace(WorkspaceCreateRequest request, String ownerEmail) {
        User owner = getUserByEmail(ownerEmail);
        if (workspaceRepository.existsByNameAndOwner(request.getName(), owner)) {
            throw new DuplicateResourceException("Workspace", "name", request.getName());
        }

        Workspace workspace = Workspace.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(owner)
                .build();
        Workspace saved = workspaceRepository.save(workspace);

        // Add owner as a member
        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(saved)
                .user(owner)
                .role(WorkspaceRole.OWNER)
                .accepted(true)
                .build();
        workspaceMemberRepository.save(member);

        log.info("Workspace created: {} by owner: {}", saved.getName(), ownerEmail);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspace(Long workspaceId, String userEmail) {
        User user = getUserByEmail(userEmail);
        Workspace workspace = getWorkspaceEntity(workspaceId);
        checkWorkspaceAccess(workspace, user);
        return mapToResponse(workspace);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WorkspaceResponse> getUserWorkspaces(String userEmail, Pageable pageable) {
        User user = getUserByEmail(userEmail);
        return workspaceRepository.findAllAccessibleByUser(user, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public WorkspaceResponse updateWorkspace(Long workspaceId, WorkspaceCreateRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);
        Workspace workspace = getWorkspaceEntity(workspaceId);
        checkWorkspaceOwner(workspace, user);

        workspace.setName(request.getName());
        workspace.setDescription(request.getDescription());
        Workspace updated = workspaceRepository.save(workspace);

        log.info("Workspace updated: {}", updated.getName());
        return mapToResponse(updated);
    }

    @Override
    public void deleteWorkspace(Long workspaceId, String userEmail) {
        User user = getUserByEmail(userEmail);
        Workspace workspace = getWorkspaceEntity(workspaceId);
        checkWorkspaceOwner(workspace, user);

        workspaceRepository.delete(workspace);
        log.info("Workspace deleted: {}", workspaceId);
    }

    @Override
    public WorkspaceResponse inviteMember(Long workspaceId, InviteMemberRequest request, String inviterEmail) {
        User inviter = getUserByEmail(inviterEmail);
        Workspace workspace = getWorkspaceEntity(workspaceId);
        checkWorkspaceOwnerOrEditor(workspace, inviter);

        User invitee = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        if (workspaceMemberRepository.existsByWorkspaceAndUser(workspace, invitee)) {
            throw new BadRequestException("User is already a member or invited to this workspace");
        }

        WorkspaceMember workspaceMember = WorkspaceMember.builder()
                .workspace(workspace)
                .user(invitee)
                .role(request.getRole())
                .accepted(false)
                .build();
        workspaceMemberRepository.save(workspaceMember);

        // Send Notification
        notificationService.createAndPushNotification(
                invitee, inviter, NotificationType.WORKSPACE_INVITATION,
                "Workspace Invitation",
                inviter.getFullName() + " invited you to join workspace \"" + workspace.getName() + "\"",
                "/workspaces/" + workspaceId,
                workspaceId, "Workspace"
        );

        log.info("User {} invited to workspace {} by {}", invitee.getEmail(), workspace.getName(), inviterEmail);
        return mapToResponse(workspace);
    }

    @Override
    public WorkspaceResponse acceptInvitation(Long workspaceId, String userEmail) {
        User user = getUserByEmail(userEmail);
        Workspace workspace = getWorkspaceEntity(workspaceId);

        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceAndUser(workspace, user)
                .orElseThrow(() -> new BadRequestException("No invitation found for this workspace"));

        if (member.isAccepted()) {
            throw new BadRequestException("Invitation already accepted");
        }

        member.setAccepted(true);
        workspaceMemberRepository.save(member);

        // Notify workspace owner
        notificationService.createAndPushNotification(
                workspace.getOwner(), user, NotificationType.SYSTEM,
                "Invitation Accepted",
                user.getFullName() + " accepted your invitation to join \"" + workspace.getName() + "\"",
                "/workspaces/" + workspaceId,
                workspaceId, "Workspace"
        );

        log.info("User {} accepted invitation to workspace {}", userEmail, workspace.getName());
        return mapToResponse(workspace);
    }

    @Override
    public void removeMember(Long workspaceId, Long memberUserId, String requesterEmail) {
        User requester = getUserByEmail(requesterEmail);
        Workspace workspace = getWorkspaceEntity(workspaceId);

        User memberUser = userRepository.findById(memberUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", memberUserId));

        // Only owner can remove someone, or user can remove themselves (leave workspace)
        boolean isOwner = workspace.getOwner().getId().equals(requester.getId());
        boolean isSelf = requester.getId().equals(memberUserId);

        if (!isOwner && !isSelf) {
            throw new UnauthorizedException("Only the workspace owner can remove members");
        }

        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceAndUser(workspace, memberUser)
                .orElseThrow(() -> new ResourceNotFoundException("WorkspaceMember", "userId", memberUserId.toString()));

        if (member.getRole() == WorkspaceRole.OWNER) {
            throw new BadRequestException("Cannot remove the workspace owner");
        }

        workspaceMemberRepository.delete(member);

        if (isSelf) {
            log.info("User {} left workspace {}", requesterEmail, workspace.getName());
        } else {
            log.info("User {} removed from workspace {} by {}", memberUser.getEmail(), workspace.getName(), requesterEmail);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getMembers(Long workspaceId, String requesterEmail) {
        User requester = getUserByEmail(requesterEmail);
        Workspace workspace = getWorkspaceEntity(workspaceId);
        checkWorkspaceAccess(workspace, requester);

        return workspaceMemberRepository.findByWorkspace(workspace).stream()
                .map(m -> UserResponse.fromEntity(m.getUser()))
                .collect(Collectors.toList());
    }

    // ===== Helpers =====
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private Workspace getWorkspaceEntity(Long id) {
        return workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", id));
    }

    private void checkWorkspaceAccess(Workspace workspace, User user) {
        boolean isOwner = workspace.getOwner().getId().equals(user.getId());
        boolean isMember = workspaceMemberRepository.existsByWorkspaceAndUser(workspace, user);
        if (!isOwner && !isMember) {
            throw new UnauthorizedException("Access denied to this workspace");
        }
    }

    private void checkWorkspaceOwner(Workspace workspace, User user) {
        if (!workspace.getOwner().getId().equals(user.getId())) {
            throw new UnauthorizedException("Only the workspace owner can perform this action");
        }
    }

    private void checkWorkspaceOwnerOrEditor(Workspace workspace, User user) {
        boolean isOwner = workspace.getOwner().getId().equals(user.getId());
        Optional<WorkspaceMember> memberOpt = workspaceMemberRepository.findByWorkspaceAndUser(workspace, user);
        boolean isEditor = memberOpt.isPresent() && memberOpt.get().getRole() == WorkspaceRole.EDITOR && memberOpt.get().isAccepted();

        if (!isOwner && !isEditor) {
            throw new UnauthorizedException("You must be an Owner or Editor to invite members");
        }
    }

    private WorkspaceResponse mapToResponse(Workspace workspace) {
        long memberCount = workspaceMemberRepository.countByWorkspace(workspace);
        long blogCount = blogRepository.countByWorkspaceAndStatus(workspace, null); // count all blogs
        return WorkspaceResponse.fromEntity(workspace, memberCount, blogCount);
    }
}
