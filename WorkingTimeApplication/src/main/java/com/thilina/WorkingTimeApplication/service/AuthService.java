package com.thilina.WorkingTimeApplication.service;

import com.thilina.WorkingTimeApplication.dto.AuthRequest;
import com.thilina.WorkingTimeApplication.dto.AuthResponse;
import com.thilina.WorkingTimeApplication.dto.RegisterRequest;

public interface AuthService {

    AuthResponse login(AuthRequest request);
    AuthResponse register(RegisterRequest request);

    void logout(String authorizationHeader);

    AuthResponse refreshToken(String authorizationHeader);
}
