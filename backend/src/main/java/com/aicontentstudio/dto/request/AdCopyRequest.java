package com.aicontentstudio.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdCopyRequest {
    @NotBlank(message = "Platform is required (e.g. GOOGLE, FACEBOOK, LINKEDIN)")
    private String platform;

    @NotBlank(message = "Product name is required")
    private String productName;

    @NotBlank(message = "Product description is required")
    private String description;

    private String targetAudience;
}
