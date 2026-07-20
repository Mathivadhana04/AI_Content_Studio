package com.aicontentstudio.dto.response;

import com.aicontentstudio.entity.User;
import com.aicontentstudio.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private UserRole role;
    private String bio;
    private String avatarUrl;
    private boolean active;
    private LocalDateTime createdAt;

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .role(user.getRole())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
