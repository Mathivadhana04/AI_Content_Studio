package com.aicontentstudio.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CommentRequest {

    @NotBlank(message = "Comment content is required")
    private String content;

    private Long parentId;

    private List<Long> mentionedUserIds;
}
