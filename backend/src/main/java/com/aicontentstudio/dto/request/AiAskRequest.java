package com.aicontentstudio.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiAskRequest {
    @NotNull(message = "Blog ID is required")
    private Long blogId;

    @NotBlank(message = "Question is required")
    private String question;
}
