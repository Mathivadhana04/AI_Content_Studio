package com.aicontentstudio.service.impl;

import com.aicontentstudio.dto.request.RegisterRequest;
import com.aicontentstudio.dto.response.UserResponse;
import com.aicontentstudio.entity.User;
import com.aicontentstudio.exception.BadRequestException;
import com.aicontentstudio.exception.ResourceNotFoundException;
import com.aicontentstudio.repository.UserRepository;
import com.aicontentstudio.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = getUserByEmail(email);
        return UserResponse.fromEntity(user);
    }

    @Override
    public UserResponse updateProfile(String email, RegisterRequest request) {
        User user = getUserByEmail(email);

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        User updated = userRepository.save(user);
        log.info("Profile updated for user: {}", email);
        return UserResponse.fromEntity(updated);
    }

    @Override
    public void changePassword(String email, String oldPassword, String newPassword) {
        User user = getUserByEmail(email);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Current password verification failed");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed successfully for user: {}", email);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponse::fromEntity);
    }

    @Override
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setActive(false);
        userRepository.save(user);
        log.info("User account {} deactivated", userId);
    }

    @Override
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setActive(true);
        userRepository.save(user);
        log.info("User account {} activated", userId);
    }

    // ===== Helpers =====
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}
