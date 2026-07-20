package com.aicontentstudio.dto.response;

import com.aicontentstudio.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long accessTokenExpiresIn;
    private UserResponse user;

    public static AuthResponse of(String accessToken, String refreshToken, UserResponse user, long expiresIn) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .accessTokenExpiresIn(expiresIn)
                .user(user)
                .build();
    }
}
