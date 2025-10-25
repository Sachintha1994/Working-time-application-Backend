package com.thilina.WorkingTimeApplication.service;

import com.thilina.WorkingTimeApplication.dto.*;

import java.util.List;

public interface TaskService {
    TaskResponse createTask(TaskRequest request, String username);
    List<TaskResponse> getTasksForUser(String username, String role);
    TaskResponse getTaskById(Long id, String username);
    TaskResponse submitTimeEstimate(Long id, TimeEstimateRequest request, String username);
    EndDateCalculationResponse calculateEndDate(Long id, EndDateCalculationRequest request, String username);
    TaskResponse updateTask(Long id, TaskRequest request, String username);
    void deleteTask(Long id, String username);
}
