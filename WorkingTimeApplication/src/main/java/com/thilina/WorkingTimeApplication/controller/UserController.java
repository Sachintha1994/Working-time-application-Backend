package com.thilina.WorkingTimeApplication.controller;


import com.thilina.WorkingTimeApplication.dto.UserResponse;
import com.thilina.WorkingTimeApplication.service.UserService;
import com.thilina.WorkingTimeApplication.util.response.SuccessResponseWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
     * Fetch all engineers
     * GET /api/users/engineers
     */
    @GetMapping("/engineers")
    @PreAuthorize("hasRole('PROJECT_MANAGER')") // optional: restrict to PM only
    public ResponseEntity<SuccessResponseWrapper<List<UserResponse>>> getAllEngineers() {
        List<UserResponse> engineers = userService.getAllEngineers();
        return new ResponseEntity<>(new SuccessResponseWrapper<>(engineers), HttpStatus.OK);

    }
}