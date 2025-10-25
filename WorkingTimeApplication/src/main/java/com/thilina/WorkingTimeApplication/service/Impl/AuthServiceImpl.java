package com.thilina.WorkingTimeApplication.service.Impl;

import com.thilina.WorkingTimeApplication.config.jwt.JwtUtil;
import com.thilina.WorkingTimeApplication.dto.AuthRequest;
import com.thilina.WorkingTimeApplication.dto.AuthResponse;
import com.thilina.WorkingTimeApplication.dto.RegisterRequest;
import com.thilina.WorkingTimeApplication.enums.Role;
import com.thilina.WorkingTimeApplication.model.User;
import com.thilina.WorkingTimeApplication.model.UserSession;
import com.thilina.WorkingTimeApplication.repository.UserRepository;
import com.thilina.WorkingTimeApplication.repository.UserSessionRepository;
import com.thilina.WorkingTimeApplication.service.AuthService;
import com.thilina.WorkingTimeApplication.util.exception.ValidationException;
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
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponse login(AuthRequest request) {
        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // Find user
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Handle user session
        UserSession userSession = user.getUserSession();
        if (userSession == null || !userSession.isActive()) {
            userSession = createUserSession(userSession, user);
            user.setUserSession(userSession);
        } else {
            throw new ValidationException("login", "userSession.exist");
        }

        // Save user with session
        userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }
    private UserSession createUserSession(UserSession existingSession, User user) {
        UserSession userSession = existingSession != null ? existingSession : new UserSession();

        userSession.setUser(user);
        userSession.setActive(true);

        return userSessionRepository.save(userSession);
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNo(request.getPhoneNo());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ENGINEER); // Default role for registration

        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }

    @Override
    public void logout(String authorizationHeader) {

    }
}
