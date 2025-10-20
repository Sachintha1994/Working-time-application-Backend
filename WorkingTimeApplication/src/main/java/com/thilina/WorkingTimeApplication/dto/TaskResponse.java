package com.thilina.WorkingTimeApplication.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private Long assignedToId;
    private String assignedToUsername;
    private Long createdById;
    private String createdByUsername;
    private Double timeEstimate;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
