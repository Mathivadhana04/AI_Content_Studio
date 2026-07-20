package com.aicontentstudio.dto.request;

import com.aicontentstudio.enums.AiTone;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiRewriteRequest {

    @NotBlank(message = "Text to rewrite is required")
    private String text;

    @NotNull(message = "Tone is required")
    private AiTone tone;

    private Long blogId;
}
