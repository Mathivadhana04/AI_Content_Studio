package com.aicontentstudio.dto.request;

import com.aicontentstudio.enums.EmailType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmailContentRequest {

    @NotNull(message = "Email type is required")
    private EmailType emailType;

    @NotBlank(message = "Subject is required")
    private String subject;

    private String context;

    private String targetAudience;

    private Long sourceBlogId;
}
