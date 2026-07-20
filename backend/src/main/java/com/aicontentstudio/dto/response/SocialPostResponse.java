package com.aicontentstudio.dto.response;

import com.aicontentstudio.entity.SocialPost;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialPostResponse {
    private Long id;
    private Long blogId;
    private String blogTitle;
    private String platform;
    private String content;
    private String hashtags;
    private String imagePrompt;
    private LocalDateTime createdAt;

    public static SocialPostResponse fromEntity(SocialPost post) {
        if (post == null) return null;
        return SocialPostResponse.builder()
                .id(post.getId())
                .blogId(post.getBlog() != null ? post.getBlog().getId() : null)
                .blogTitle(post.getBlog() != null ? post.getBlog().getTitle() : null)
                .platform(post.getPlatform() != null ? post.getPlatform().name() : null)
                .content(post.getContent())
                .hashtags(post.getHashtags())
                .imagePrompt(post.getImagePrompt())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
