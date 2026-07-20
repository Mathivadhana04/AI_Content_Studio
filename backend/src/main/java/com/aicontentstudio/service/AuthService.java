package com.aicontentstudio.service;

import com.aicontentstudio.dto.request.LoginRequest;
import com.aicontentstudio.dto.request.RefreshTokenRequest;
import com.aicontentstudio.dto.request.RegisterRequest;
import com.aicontentstudio.dto.response.AuthResponse;
import com.aicontentstudio.dto.response.MessageResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    MessageResponse logout(String refreshToken);
}
