package com.aicontentstudio.dto.response;

import com.aicontentstudio.entity.Blog;
import com.aicontentstudio.enums.BlogStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BlogSummaryResponse {
    private Long id;
    private String title;
    private String metaDescription;
    private BlogStatus status;
    private int wordCount;
    private int seoScore;
    private boolean aiGenerated;
    private String authorName;
    private Long workspaceId;
    private String workspaceName;
    private LocalDateTime scheduledAt;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BlogSummaryResponse fromEntity(Blog blog) {
        return BlogSummaryResponse.builder()
                .id(blog.getId())
                .title(blog.getTitle())
                .metaDescription(blog.getMetaDescription())
                .status(blog.getStatus())
                .wordCount(blog.getWordCount())
                .seoScore(blog.getSeoScore())
                .aiGenerated(blog.isAiGenerated())
                .authorName(blog.getAuthor().getFullName())
                .workspaceId(blog.getWorkspace().getId())
                .workspaceName(blog.getWorkspace().getName())
                .scheduledAt(blog.getScheduledAt())
                .publishedAt(blog.getPublishedAt())
                .createdAt(blog.getCreatedAt())
                .updatedAt(blog.getUpdatedAt())
                .build();
    }
}
