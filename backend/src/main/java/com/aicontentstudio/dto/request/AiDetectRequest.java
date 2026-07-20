package com.aicontentstudio.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiDetectRequest {
    @NotBlank(message = "Text is required")
    private String text;
}
