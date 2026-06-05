package com.aicontentstudio.auth.service;

import com.aicontentstudio.auth.dto.AuthResponse;
import com.aicontentstudio.auth.dto.LoginRequest;
import com.aicontentstudio.auth.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
