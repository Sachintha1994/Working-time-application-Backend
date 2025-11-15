package com.thilina.WorkingTimeApplication.controller;

import com.thilina.WorkingTimeApplication.dto.AuthRequest;
import com.thilina.WorkingTimeApplication.dto.AuthResponse;
import com.thilina.WorkingTimeApplication.dto.RegisterRequest;
import com.thilina.WorkingTimeApplication.service.AuthService;
import com.thilina.WorkingTimeApplication.util.response.SuccessResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "User Login",
            description = "Authenticates a user (PM or Engineer) and returns a JWT token"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Successfully authenticated"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Invalid request format")
    })
    @PostMapping(
            value = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SuccessResponseWrapper<AuthResponse>> login(
            @Valid
            @RequestBody
            @Parameter(description = "Login credentials", required = true)
            AuthRequest request) {

        log.info("Login attempt for user: {}", request.getUsername());

        AuthResponse authResponse = authService.login(request);

        log.info("User {} logged in successfully", request.getUsername());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new SuccessResponseWrapper<>(authResponse));
    }

    /**
     * Registers a new engineer user
     *
     * @param request Registration details
     * @return AuthResponse containing JWT token and user details
     */
    @Operation(
            summary = "Engineer Registration",
            description = "Registers a new engineer user and returns a JWT token"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully registered"),
            @ApiResponse(responseCode = "400", description = "Invalid request or username already exists"),
            @ApiResponse(responseCode = "409", description = "Username already exists")
    })
    @PostMapping(
            value = "/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SuccessResponseWrapper<AuthResponse>> register(
            @Valid
            @RequestBody
            @Parameter(description = "Registration details", required = true)
            RegisterRequest request) {

        log.info("Registration attempt for user: {}", request.getUsername());

        AuthResponse authResponse = authService.register(request);

        log.info("User {} registered successfully", request.getUsername());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new SuccessResponseWrapper<>(authResponse));
    }

    /**
     * Logs out the current user
     *
     * @param authorizationHeader JWT token in Authorization header
     * @return Success message
     */
    @Operation(
            summary = "User Logout",
            description = "Logs out the current user and invalidates the JWT token",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully logged out"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    @PostMapping(
            value = "/logout",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponseWrapper<String>> logout(
            @RequestHeader("Authorization")
            @Parameter(description = "JWT token", required = true, example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            String authorizationHeader) {

        log.info("Logout request received");

        authService.logout(authorizationHeader);

        log.info("User logged out successfully");

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new SuccessResponseWrapper<>("Logged out successfully"));
    }

    /**
     * Refreshes JWT token
     *
     * @param authorizationHeader Current JWT token
     * @return New JWT token
     */
    @Operation(
            summary = "Refresh Token",
            description = "Generates a new JWT token using the current valid token",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired token")
    })
    @PostMapping(
            value = "/refresh",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponseWrapper<AuthResponse>> refreshToken(
            @RequestHeader("Authorization")
            @Parameter(description = "JWT token", required = true)
            String authorizationHeader) {

        log.info("Token refresh request received");

        AuthResponse authResponse = authService.refreshToken(authorizationHeader);

        log.info("Token refreshed successfully");

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new SuccessResponseWrapper<>(authResponse));
    }

    /**
     * Health check endpoint for authentication service
     *
     * @return Service status
     */
    @Operation(
            summary = "Health Check",
            description = "Checks if the authentication service is running"
    )
    @GetMapping(
            value = "/health",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SuccessResponseWrapper<String>> health() {
        return ResponseEntity
                .ok(new SuccessResponseWrapper<>("Authentication service is running"));
    }

}
