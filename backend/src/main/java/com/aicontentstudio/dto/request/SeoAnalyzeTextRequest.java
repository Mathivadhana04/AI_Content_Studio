package com.aicontentstudio.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SeoAnalyzeTextRequest {
    @NotBlank(message = "Text cannot be blank")
    private String text;

    private String targetKeyword;
}
