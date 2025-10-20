package com.thilina.WorkingTimeApplication.service.Impl;

import com.thilina.WorkingTimeApplication.dto.*;
import com.thilina.WorkingTimeApplication.enums.Role;
import com.thilina.WorkingTimeApplication.enums.TaskStatus;
import com.thilina.WorkingTimeApplication.model.Task;
import com.thilina.WorkingTimeApplication.model.User;
import com.thilina.WorkingTimeApplication.repository.TaskRepository;
import com.thilina.WorkingTimeApplication.service.TaskService;
import com.thilina.WorkingTimeApplication.service.TimeCalculationService;
import com.thilina.WorkingTimeApplication.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final UserService userService;
    private final TimeCalculationService timeCalculationService;

    @Override
    @Transactional
    public TaskResponse createTask(TaskRequest request, String username) {
        User pm = userService.getUserByUsername(username);

        if (pm.getRole() != Role.PROJECT_MANAGER) {
            throw new RuntimeException("Only Project Managers can create tasks");
        }

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setCreatedBy(pm);

        if (request.getAssignedToId() != null) {
            User engineer = userService.getUserById(request.getAssignedToId());
            if (engineer.getRole() != Role.ENGINEER) {
                throw new RuntimeException("Tasks can only be assigned to Engineers");
            }
            task.setAssignedTo(engineer);
        }

        task = taskRepository.save(task);
        return mapToResponse(task);
    }

    @Override
    public TaskResponse getTaskById(Long id, String username) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        User user = userService.getUserByUsername(username);

        // Engineers can only see their own tasks
        if (user.getRole() == Role.ENGINEER &&
                (task.getAssignedTo() == null || !task.getAssignedTo().getId().equals(user.getId()))) {
            throw new RuntimeException("You don't have permission to view this task");
        }

        return mapToResponse(task);
    }

    @Override
    public List<TaskResponse> getTasksForUser(String username, String role) {
        User user = userService.getUserByUsername(username);
        List<Task> tasks;

        if (user.getRole() == Role.ENGINEER) {
            tasks = taskRepository.findByAssignedTo(user);
        } else {
            tasks = taskRepository.findByCreatedBy(user);
        }

        return tasks.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TaskResponse submitTimeEstimate(Long taskId, TimeEstimateRequest request, String username) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        User engineer = userService.getUserByUsername(username);

        if (engineer.getRole() != Role.ENGINEER) {
            throw new RuntimeException("Only Engineers can submit time estimates");
        }

        if (task.getAssignedTo() == null || !task.getAssignedTo().getId().equals(engineer.getId())) {
            throw new RuntimeException("You can only estimate tasks assigned to you");
        }

        task.setTimeEstimate(request.getEstimateDays());
        task.setStatus(TaskStatus.ESTIMATED);

        task = taskRepository.save(task);
        return mapToResponse(task);
    }

    @Override
    @Transactional
    public EndDateCalculationResponse calculateEndDate(Long taskId, EndDateCalculationRequest request, String username) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        User pm = userService.getUserByUsername(username);

        if (pm.getRole() != Role.PROJECT_MANAGER) {
            throw new RuntimeException("Only Project Managers can calculate end dates");
        }

        if (!task.getCreatedBy().getId().equals(pm.getId())) {
            throw new RuntimeException("You can only calculate end dates for your own tasks");
        }

        if (task.getTimeEstimate() == null) {
            throw new RuntimeException("Task must have a time estimate before calculating end date");
        }

        LocalDateTime endDateTime = timeCalculationService.calculateEndDateTime(
                request.getStartDateTime(),
                task.getTimeEstimate()
        );

        task.setStartDateTime(request.getStartDateTime());
        task.setEndDateTime(endDateTime);

        taskRepository.save(task);

        return new EndDateCalculationResponse(endDateTime);
    }

    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .assignedToId(task.getAssignedTo() != null ? task.getAssignedTo().getId() : null)
                .assignedToUsername(task.getAssignedTo() != null ? task.getAssignedTo().getUsername() : null)
                .createdById(task.getCreatedBy().getId())
                .createdByUsername(task.getCreatedBy().getUsername())
                .timeEstimate(task.getTimeEstimate())
                .startDateTime(task.getStartDateTime())
                .endDateTime(task.getEndDateTime())
                .status(task.getStatus().name())
                .build();
    }
}
