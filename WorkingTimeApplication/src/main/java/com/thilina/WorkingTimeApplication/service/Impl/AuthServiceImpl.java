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
import com.thilina.WorkingTimeApplication.util.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public AuthResponse login(AuthRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());

        try {
            // 1. Authenticate credentials
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            log.debug("Authentication successful for username: {}", request.getUsername());
        } catch (Exception e) {
            log.error("Authentication failed for username: {}", request.getUsername(), e);
            throw e;
        }

        // 2. Load user entity
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.error("User not found with username: {}", request.getUsername());
                    return new ResourceNotFoundException("User not found");
                });

        log.debug("User found: {} with role: {}", user.getUsername(), user.getRole());

        // 3. Deactivate any existing active sessions
        deactivateExistingSessions(user);

        // 4. Create new active session
        UserSession newSession = createNewSession(user);

        // 5. Attach session to user (for cascade)
        user.getUserSessions().add(newSession);
        userRepository.save(user);

        // 6. Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        newSession.setAccessToken(token); // transient, not persisted

        log.info("User {} logged in successfully with role: {}", user.getUsername(), user.getRole());

        // 7. Return response
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }


    private void deactivateExistingSessions(User user) {
        log.debug("Checking for active sessions for user: {}", user.getUsername());

        List<UserSession> activeSessions = user.getUserSessions().stream()
                .filter(UserSession::isActive)
                .toList();

        if (activeSessions.isEmpty()) {
            log.debug("No active sessions found for user: {}", user.getUsername());
            return;
        }

        for (UserSession session : activeSessions) {
            session.setActive(false);
            userSessionRepository.save(session);
            log.info("Deactivated session ID: {} for user: {}", session.getId(), user.getUsername());
        }
    }

    private UserSession createNewSession(User user) {
        log.debug("Creating new user session for user: {}", user.getUsername());

        UserSession userSession = new UserSession();
        userSession.setUser(user);
        userSession.setActive(true);

        UserSession savedSession = userSessionRepository.save(userSession);
        log.info("New session created successfully. Session ID: {} for user: {}", savedSession.getId(), user.getUsername());

        return savedSession;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registration attempt for username: {}", request.getUsername());

        // Check for existing username
        if (userRepository.existsByUsername(request.getUsername())) {
            log.error("Registration failed - Username already exists: {}", request.getUsername());
            throw new RuntimeException("Username already exists");
        }

        log.debug("Username available: {}", request.getUsername());

        // Create new user
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNo(request.getPhoneNo());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ENGINEER); // Default role for registration

        log.debug("User object created for registration: {}", request.getUsername());

        // Save user
        user = userRepository.save(user);
        log.info("User registered successfully. User ID: {}, Username: {}, Role: {}",
                user.getId(), user.getUsername(), user.getRole());

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        log.debug("JWT token generated for newly registered user: {}", user.getUsername());

        log.info("Registration completed successfully for username: {}", user.getUsername());

        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }

    @Override
    @Transactional
    public void logout(String authorizationHeader) {
        log.info("Logout attempt received");

        try {
            // Extract token from "Bearer <token>"
            String token = authorizationHeader.substring(7);
            log.debug("Token extracted from authorization header");

            // Extract username from token
            String username = jwtUtil.extractUsername(token);
            log.info("Logout request for username: {}", username);

            // Find user
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        log.error("Logout failed - User not found with username: {}", username);
                        return new ResourceNotFoundException("User not found");
                    });

            log.debug("User found for logout: {}", username);

            // Deactivate user session
            deactivateExistingSessions(user);

            log.info("User {} logged out successfully", username);

        } catch (Exception e) {
            log.error("Logout failed with error: {}", e.getMessage(), e);
            throw e;
        }
    }
}
