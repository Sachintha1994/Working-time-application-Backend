package com.thilina.WorkingTimeApplication.controller;

import com.thilina.WorkingTimeApplication.dto.AuthRequest;
import com.thilina.WorkingTimeApplication.dto.AuthResponse;
import com.thilina.WorkingTimeApplication.dto.RegisterRequest;
import com.thilina.WorkingTimeApplication.service.AuthService;
import com.thilina.WorkingTimeApplication.util.response.SuccessResponseWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Login endpoint for both PM and Engineer
     * POST /api/auth/login
     */
    @PostMapping(path = "/login")
    public ResponseEntity<SuccessResponseWrapper<AuthResponse>> login(
            @Validated @RequestBody AuthRequest request) {
        AuthResponse authResponse = authService.login(request);
        return new ResponseEntity<>(new SuccessResponseWrapper<>(authResponse), HttpStatus.ACCEPTED);
    }

    /**
     * Register endpoint for Engineers only
     * POST /api/auth/register
     */
    @PostMapping(path = "/register")
    public ResponseEntity<SuccessResponseWrapper<AuthResponse>> register(
            @Validated @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);
        return new ResponseEntity<>(new SuccessResponseWrapper<>(authResponse), HttpStatus.CREATED);
    }


    /**
     * Logout endpoint
     * POST /api/auth/logout
     */
    @PostMapping(path = "/logout")
    public ResponseEntity<SuccessResponseWrapper<String>> logout(
            @RequestHeader("Authorization") String authorizationHeader) {
        authService.logout(authorizationHeader);
        return new ResponseEntity<>(new SuccessResponseWrapper<>("Logged out successfully"), HttpStatus.ACCEPTED);
    }

}
