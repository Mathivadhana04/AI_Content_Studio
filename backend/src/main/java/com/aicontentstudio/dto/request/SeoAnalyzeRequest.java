package com.aicontentstudio.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SeoAnalyzeRequest {

    @NotNull(message = "Blog ID is required")
    private Long blogId;

    private String targetKeyword;
}
