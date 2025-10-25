package com.thilina.WorkingTimeApplication.service.Impl;

import com.thilina.WorkingTimeApplication.dto.*;
import com.thilina.WorkingTimeApplication.enums.Role;
import com.thilina.WorkingTimeApplication.enums.TaskStatus;
import com.thilina.WorkingTimeApplication.model.Task;
import com.thilina.WorkingTimeApplication.model.User;
import com.thilina.WorkingTimeApplication.repository.TaskRepository;
import com.thilina.WorkingTimeApplication.repository.UserRepository;
import com.thilina.WorkingTimeApplication.service.TaskService;
import com.thilina.WorkingTimeApplication.service.TimeCalculationService;
import com.thilina.WorkingTimeApplication.service.UserService;
import com.thilina.WorkingTimeApplication.util.exception.RequiredFieldException;
import com.thilina.WorkingTimeApplication.util.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final TimeCalculationService timeCalculationService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public TaskResponse createTask(TaskRequest request, String username) {
        User pm = userService.getUserByUsername(username);

        if (pm.getRole() != Role.PROJECT_MANAGER) {
            throw new AccessDeniedException("Only Project Managers can create tasks");
        }

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setCreatedBy(pm);
        task.setStatus(TaskStatus.CREATED);

        if (request.getAssignedToId() != null) {
            User engineer = userService.getUserById(request.getAssignedToId());
            if (engineer.getRole() != Role.ENGINEER) {
                throw new AccessDeniedException("Tasks can only be assigned to Engineers");
            }
            task.setAssignedTo(engineer);
            task.setStatus(TaskStatus.ASSIGNED);
        }

        task = taskRepository.save(task);
        return mapToResponse(task);
    }

    @Override
    public TaskResponse getTaskById(Long id, String username) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        User user = userService.getUserByUsername(username);

        // Engineers can only see their own tasks
        if (user.getRole() == Role.ENGINEER &&
                (task.getAssignedTo() == null || !task.getAssignedTo().getId().equals(user.getId()))) {
            throw new AccessDeniedException("You don't have permission to view this task");
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
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        User engineer = userService.getUserByUsername(username);

        if (engineer.getRole() != Role.ENGINEER) {
            throw new AccessDeniedException("Only Engineers can submit time estimates");
        }

        if (task.getAssignedTo() == null || !task.getAssignedTo().getId().equals(engineer.getId())) {
            throw new AccessDeniedException("You can only estimate tasks assigned to you");
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
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        User pm = userService.getUserByUsername(username);

        if (pm.getRole() != Role.PROJECT_MANAGER) {
            throw new AccessDeniedException("Only Project Managers can calculate end dates");
        }

        if (!task.getCreatedBy().getId().equals(pm.getId())) {
            throw new AccessDeniedException("You can only calculate end dates for your own tasks");
        }

        if (task.getTimeEstimate() == null) {
            throw new RequiredFieldException("Task must have a time estimate before calculating end date");
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

    public TaskResponse updateTask(Long id, TaskRequest request, String username) {
        // Find task
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        // Verify user is the creator (PM)
        if (!task.getCreatedBy().getUsername().equals(username)) {
            throw new AccessDeniedException("Access denied: You can only update your own tasks");
        }

        // Update task fields
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());

        // Update assigned engineer if changed
        if (request.getAssignedToId() != null) {
            User engineer = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("Engineer not found"));
            task.setAssignedTo(engineer);
        }

        Task updatedTask = taskRepository.save(task);
        return modelMapper.map(updatedTask, TaskResponse.class);
    }

    @Override
    public void deleteTask(Long id, String username) {
        // Find task
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        // Verify user is the creator (PM)
        if (!task.getCreatedBy().getUsername().equals(username)) {
            throw new AccessDeniedException("Access denied: You can only delete your own tasks");
        }

        // Delete the task
        taskRepository.delete(task);
    }
}
