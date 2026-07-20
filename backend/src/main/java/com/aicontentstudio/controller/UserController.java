package com.aicontentstudio.controller;

import com.aicontentstudio.dto.request.RegisterRequest;
import com.aicontentstudio.dto.response.MessageResponse;
import com.aicontentstudio.dto.response.UserResponse;
import com.aicontentstudio.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile and details settings")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile details")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getCurrentUser(userDetails.getUsername()));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile info (names, bio, avatar)")
    public ResponseEntity<UserResponse> updateProfile(
            @Valid @RequestBody RegisterRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.updateProfile(userDetails.getUsername(), request));
    }

    @PutMapping("/me/password")
    @Operation(summary = "Change user account password")
    public ResponseEntity<MessageResponse> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @AuthenticationPrincipal UserDetails userDetails) {
        userService.changePassword(userDetails.getUsername(), oldPassword, newPassword);
        return ResponseEntity.ok(MessageResponse.of("Password updated successfully"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get list of all registered users (ADMIN only)")
    public ResponseEntity<Page<UserResponse>> listUsers(
            @PageableDefault(size = 15) Pageable pageable) {
        return ResponseEntity.ok(userService.listUsers(pageable));
    }

    @DeleteMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate a user account (ADMIN only)")
    public ResponseEntity<MessageResponse> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(MessageResponse.of("User account deactivated successfully"));
    }
}
