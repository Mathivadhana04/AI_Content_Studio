package com.aicontentstudio.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageResponse {
    private String message;

    public static MessageResponse of(String message) {
        return MessageResponse.builder().message(message).build();
    }
}
