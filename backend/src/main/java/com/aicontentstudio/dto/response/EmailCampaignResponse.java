package com.aicontentstudio.dto.response;

import com.aicontentstudio.entity.EmailCampaign;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailCampaignResponse {
    private Long id;
    private Long sourceBlogId;
    private String sourceBlogTitle;
    private String emailType;
    private String subject;
    private String htmlContent;
    private String plainTextContent;
    private String targetAudience;
    private LocalDateTime createdAt;

    public static EmailCampaignResponse fromEntity(EmailCampaign c) {
        if (c == null) return null;
        return EmailCampaignResponse.builder()
                .id(c.getId())
                .sourceBlogId(c.getSourceBlog() != null ? c.getSourceBlog().getId() : null)
                .sourceBlogTitle(c.getSourceBlog() != null ? c.getSourceBlog().getTitle() : null)
                .emailType(c.getEmailType() != null ? c.getEmailType().name() : null)
                .subject(c.getSubject())
                .htmlContent(c.getHtmlContent())
                .plainTextContent(c.getPlainTextContent())
                .targetAudience(c.getTargetAudience())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
