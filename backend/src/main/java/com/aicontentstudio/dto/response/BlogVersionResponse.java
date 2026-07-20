package com.aicontentstudio.dto.response;

import com.aicontentstudio.entity.BlogVersion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogVersionResponse {
    private Long id;
    private int versionNumber;
    private String title;
    private String content;
    private String changeNote;
    private String savedByEmail;
    private LocalDateTime createdAt;

    public static BlogVersionResponse fromEntity(BlogVersion version) {
        return BlogVersionResponse.builder()
                .id(version.getId())
                .versionNumber(version.getVersionNumber())
                .title(version.getTitle())
                .content(version.getContent())
                .changeNote(version.getChangeNote())
                .savedByEmail(version.getSavedBy() != null ? version.getSavedBy().getEmail() : null)
                .createdAt(version.getCreatedAt())
                .build();
    }
}
