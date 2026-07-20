package com.aicontentstudio.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatbotRequest {
    @NotBlank(message = "Message cannot be blank")
    private String message;

    private String contextType; // "NONE", "BLOG", "SOCIAL", "EMAIL"
    
    private Long contextId;
}
