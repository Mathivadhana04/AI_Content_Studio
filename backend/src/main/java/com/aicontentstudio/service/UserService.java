package com.aicontentstudio.service;

import com.aicontentstudio.dto.request.RegisterRequest;
import com.aicontentstudio.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * User profile and admin user-management operations.
 */
public interface UserService {

    /**
     * Return the profile of the currently authenticated user.
     */
    UserResponse getCurrentUser(String email);

    /**
     * Update the profile fields (name, bio, avatarUrl) of the current user.
     */
    UserResponse updateProfile(String email, RegisterRequest request);

    /**
     * Change the authenticated user's password after verifying the old one.
     */
    void changePassword(String email, String oldPassword, String newPassword);

    /**
     * Paginated list of all users — ADMIN only.
     */
    Page<UserResponse> listUsers(Pageable pageable);

    /**
     * Deactivate a user account — ADMIN only.
     */
    void deactivateUser(Long userId);

    /**
     * Re-activate a previously deactivated user account — ADMIN only.
     */
    void activateUser(Long userId);
}
