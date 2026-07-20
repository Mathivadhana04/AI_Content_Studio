package com.aicontentstudio.dto.request;

import com.aicontentstudio.enums.ContentPlatform;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SocialPostRequest {

    @NotNull(message = "Blog ID is required")
    private Long blogId;

    @NotNull(message = "Platform is required")
    private ContentPlatform platform;
}
