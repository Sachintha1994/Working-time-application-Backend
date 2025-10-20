package com.thilina.WorkingTimeApplication.service;

import com.thilina.WorkingTimeApplication.dto.*;

import java.util.List;

public interface TaskService {
    TaskResponse createTask(TaskRequest request, String username);
    TaskResponse getTaskById(Long id, String username);
    List<TaskResponse> getTasksForUser(String username, String role);
    TaskResponse submitTimeEstimate(Long taskId, TimeEstimateRequest request, String username);
    EndDateCalculationResponse calculateEndDate(Long taskId, EndDateCalculationRequest request, String username);
}
