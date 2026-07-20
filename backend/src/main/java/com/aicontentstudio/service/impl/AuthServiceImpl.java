package com.aicontentstudio.service.impl;

import com.aicontentstudio.dto.request.LoginRequest;
import com.aicontentstudio.dto.request.RefreshTokenRequest;
import com.aicontentstudio.dto.request.RegisterRequest;
import com.aicontentstudio.dto.response.AuthResponse;
import com.aicontentstudio.dto.response.MessageResponse;
import com.aicontentstudio.dto.response.UserResponse;
import com.aicontentstudio.entity.RefreshToken;
import com.aicontentstudio.entity.User;
import com.aicontentstudio.exception.BadRequestException;
import com.aicontentstudio.exception.DuplicateResourceException;
import com.aicontentstudio.exception.ResourceNotFoundException;
import com.aicontentstudio.repository.RefreshTokenRepository;
import com.aicontentstudio.repository.UserRepository;
import com.aicontentstudio.security.CustomUserDetailsService;
import com.aicontentstudio.security.JwtTokenProvider;
import com.aicontentstudio.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    @Value("${app.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired refresh token"));

        if (!storedToken.isValid()) {
            throw new BadRequestException("Refresh token is expired or revoked. Please login again.");
        }

        User user = storedToken.getUser();
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        return buildAuthResponse(user);
    }

    @Override
    public MessageResponse logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
        return MessageResponse.of("Logged out successfully");
    }

    // ===== Helper =====
    private AuthResponse buildAuthResponse(User user) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("userId", user.getId());

        String accessToken = jwtTokenProvider.generateAccessToken(extraClaims, userDetails);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        // Persist refresh token
        RefreshToken tokenEntity = RefreshToken.builder()
                .token(newRefreshToken)
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
                .build();
        refreshTokenRepository.save(tokenEntity);

        return AuthResponse.of(accessToken, newRefreshToken, UserResponse.fromEntity(user), accessTokenExpiration);
    }
}
