package com.thilina.WorkingTimeApplication.service.Impl;

import com.thilina.WorkingTimeApplication.config.JwtUtil;
import com.thilina.WorkingTimeApplication.dto.AuthRequest;
import com.thilina.WorkingTimeApplication.dto.AuthResponse;
import com.thilina.WorkingTimeApplication.dto.RegisterRequest;
import com.thilina.WorkingTimeApplication.enums.Role;
import com.thilina.WorkingTimeApplication.model.User;
import com.thilina.WorkingTimeApplication.repository.UserRepository;
import com.thilina.WorkingTimeApplication.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(Role.ENGINEER); // Default role for registration

        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }
}
