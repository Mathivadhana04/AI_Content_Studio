package com.aicontentstudio.dto.response;

import com.aicontentstudio.entity.Blog;
import com.aicontentstudio.enums.BlogStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BlogResponse {
    private Long id;
    private String title;
    private String metaDescription;
    private String content;
    private String keywords;
    private String targetAudience;
    private BlogStatus status;
    private String tone;
    private int wordCount;
    private int seoScore;
    private boolean aiGenerated;
    private String aiModel;
    private LocalDateTime scheduledAt;
    private LocalDateTime publishedAt;
    private UserResponse author;
    private Long workspaceId;
    private String workspaceName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BlogResponse fromEntity(Blog blog) {
        return BlogResponse.builder()
                .id(blog.getId())
                .title(blog.getTitle())
                .metaDescription(blog.getMetaDescription())
                .content(blog.getContent())
                .keywords(blog.getKeywords())
                .targetAudience(blog.getTargetAudience())
                .status(blog.getStatus())
                .tone(blog.getTone())
                .wordCount(blog.getWordCount())
                .seoScore(blog.getSeoScore())
                .aiGenerated(blog.isAiGenerated())
                .aiModel(blog.getAiModel())
                .scheduledAt(blog.getScheduledAt())
                .publishedAt(blog.getPublishedAt())
                .author(UserResponse.fromEntity(blog.getAuthor()))
                .workspaceId(blog.getWorkspace().getId())
                .workspaceName(blog.getWorkspace().getName())
                .createdAt(blog.getCreatedAt())
                .updatedAt(blog.getUpdatedAt())
                .build();
    }
}
