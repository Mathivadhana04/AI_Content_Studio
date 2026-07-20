package com.aicontentstudio.dto.response;

import com.aicontentstudio.entity.Workspace;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class WorkspaceResponse {
    private Long id;
    private String name;
    private String description;
    private String logoUrl;
    private UserResponse owner;
    private long memberCount;
    private long blogCount;
    private LocalDateTime createdAt;

    public static WorkspaceResponse fromEntity(Workspace workspace, long memberCount, long blogCount) {
        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .description(workspace.getDescription())
                .logoUrl(workspace.getLogoUrl())
                .owner(UserResponse.fromEntity(workspace.getOwner()))
                .memberCount(memberCount)
                .blogCount(blogCount)
                .createdAt(workspace.getCreatedAt())
                .build();
    }
}
