package com.aicontentstudio.blog.dto;

import com.aicontentstudio.blog.enums.BlogStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlogRequest {
    private String title;
    private String content;
    private BlogStatus status;
    private Long workspace;
    private Long author;
}