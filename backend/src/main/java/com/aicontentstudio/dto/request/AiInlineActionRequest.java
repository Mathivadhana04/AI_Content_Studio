package com.aicontentstudio.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiInlineActionRequest {

    @NotBlank(message = "Text is required")
    private String text;

    @NotBlank(message = "Action is required")
    private String action; // expand | shorten | fix_grammar | improve_seo

    @Size(max = 300)
    private String keywords; // For improve_seo action

    private Long blogId;
}
