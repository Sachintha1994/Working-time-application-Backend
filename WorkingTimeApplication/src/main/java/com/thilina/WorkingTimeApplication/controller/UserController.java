package com.thilina.WorkingTimeApplication.controller;


import com.thilina.WorkingTimeApplication.dto.UserResponse;
import com.thilina.WorkingTimeApplication.service.UserService;
import com.thilina.WorkingTimeApplication.util.response.SuccessResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Retrieves all engineers in the system
     * Only accessible by Project Managers
     *
     * @return List of all engineers with their details
     */
    @Operation(
            summary = "Get All Engineers",
            description = "Retrieves a list of all engineers registered in the system. Only accessible by Project Managers.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved list of engineers",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponseWrapper.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User does not have PROJECT_MANAGER role"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    @GetMapping(
            value = "/engineers",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public ResponseEntity<SuccessResponseWrapper<List<UserResponse>>> getAllEngineers() {
        log.info("Request received to fetch all engineers");

        List<UserResponse> engineers = userService.getAllEngineers();

        log.info("Successfully retrieved {} engineers", engineers.size());

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SuccessResponseWrapper<>(engineers));
    }
}