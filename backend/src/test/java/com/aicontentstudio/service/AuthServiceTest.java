package com.aicontentstudio.service;

import com.aicontentstudio.dto.request.LoginRequest;
import com.aicontentstudio.dto.request.RegisterRequest;
import com.aicontentstudio.dto.response.AuthResponse;
import com.aicontentstudio.entity.User;
import com.aicontentstudio.exception.DuplicateResourceException;
import com.aicontentstudio.repository.RefreshTokenRepository;
import com.aicontentstudio.repository.UserRepository;
import com.aicontentstudio.security.CustomUserDetailsService;
import com.aicontentstudio.security.JwtTokenProvider;
import com.aicontentstudio.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("encodedPassword")
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setEmail("john.doe@example.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john.doe@example.com");
        loginRequest.setPassword("password123");

        userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("john.doe@example.com")
                .password("encodedPassword")
                .authorities("ROLE_CONTENT_CREATOR")
                .build();
    }

    @Test
    void register_Success() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenReturn(user);
        when(userDetailsService.loadUserByUsername(any())).thenReturn(userDetails);
        when(jwtTokenProvider.generateAccessToken(any(), any())).thenReturn("mockAccessToken");
        when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("mockRefreshToken");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("mockAccessToken", response.getAccessToken());
        assertEquals("mockRefreshToken", response.getRefreshToken());
        assertEquals("John Doe", response.getUser().getFullName());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void register_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByEmail(any())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_Success() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(userDetailsService.loadUserByUsername(any())).thenReturn(userDetails);
        when(jwtTokenProvider.generateAccessToken(any(), any())).thenReturn("mockAccessToken");
        when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("mockRefreshToken");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mockAccessToken", response.getAccessToken());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
