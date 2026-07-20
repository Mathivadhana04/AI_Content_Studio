package com.aicontentstudio.dto.request;

import com.aicontentstudio.enums.BlogStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BlogUpdateRequest {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 300, message = "Meta description must not exceed 300 characters")
    private String metaDescription;

    private String content;

    @Size(max = 500, message = "Keywords must not exceed 500 characters")
    private String keywords;

    private BlogStatus status;

    private LocalDateTime scheduledAt;

    private String changeNote;
}
