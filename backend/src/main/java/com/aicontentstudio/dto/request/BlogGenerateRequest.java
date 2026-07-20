package com.aicontentstudio.dto.request;

import com.aicontentstudio.enums.AiTone;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BlogGenerateRequest {

    @NotBlank(message = "Topic is required")
    @Size(min = 5, max = 200, message = "Topic must be between 5 and 200 characters")
    private String topic;

    @Size(max = 300, message = "Target audience must not exceed 300 characters")
    private String targetAudience = "general audience";

    private AiTone tone = AiTone.PROFESSIONAL;

    @Size(max = 300, message = "Keywords must not exceed 300 characters")
    private String keywords = "";

    @Min(value = 300, message = "Minimum word count is 300")
    @Max(value = 5000, message = "Maximum word count is 5000")
    private int targetWordCount = 1200;

    private Long workspaceId;
}
