package com.thilina.WorkingTimeApplication.controller;

import com.thilina.WorkingTimeApplication.dto.*;
import com.thilina.WorkingTimeApplication.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * Create a new task (PM only)
     * POST /api/tasks
     *
     * Request Body:
     * {
     *   "title": "Task Title",
     *   "description": "Task Description",
     *   "assignedToId": 2
     * }
     */
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Validated @RequestBody TaskRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        TaskResponse response = taskService.createTask(request, username);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all tasks for the authenticated user
     * GET /api/tasks
     *
     * - PM: Returns all tasks they created
     * - Engineer: Returns only tasks assigned to them
     */
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasksForUser(Authentication authentication) {

        String username = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("");

        List<TaskResponse> tasks = taskService.getTasksForUser(username, role);

        return ResponseEntity.ok(tasks);
    }

    /**
     * Get a specific task by ID
     * GET /api/tasks/{id}
     *
     * Engineers can only view their assigned tasks
     * PMs can view all their created tasks
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable Long id,
            Authentication authentication) {

        String username = authentication.getName();
        TaskResponse task = taskService.getTaskById(id, username);

        return ResponseEntity.ok(task);
    }

    /**
     * Submit time estimate for a task (Engineer only)
     * PUT /api/tasks/{id}/estimate
     *
     * Request Body:
     * {
     *   "estimateDays": 5.5
     * }
     */
    @PutMapping("/{id}/estimate")
    public ResponseEntity<TaskResponse> submitTimeEstimate(
            @PathVariable Long id,
            @Validated @RequestBody TimeEstimateRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        TaskResponse response = taskService.submitTimeEstimate(id, request, username);

        return ResponseEntity.ok(response);
    }

    /**
     * Calculate end date based on start date and estimate (PM only)
     * POST /api/tasks/{id}/calculate-end-date
     *
     * Request Body:
     * {
     *   "startDateTime": "2024-05-20T08:00:00"
     * }
     *
     * Response:
     * {
     *   "endDateTime": "2024-05-28T12:00:00"
     * }
     */
    @PostMapping("/{id}/calculate-end-date")
    public ResponseEntity<EndDateCalculationResponse> calculateEndDate(
            @PathVariable Long id,
            @Validated @RequestBody EndDateCalculationRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        EndDateCalculationResponse response = taskService.calculateEndDate(id, request, username);

        return ResponseEntity.ok(response);
    }

    /**
     * Update task details (PM only)
     * PUT /api/tasks/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Validated @RequestBody TaskRequest request,
            Authentication authentication) {

        // This can be extended based on requirements
        return ResponseEntity.ok().build();
    }

    /**
     * Delete a task (PM only)
     * DELETE /api/tasks/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            Authentication authentication) {

        // This can be extended based on requirements
        return ResponseEntity.noContent().build();
    }
}

