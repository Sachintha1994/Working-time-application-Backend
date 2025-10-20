package com.thilina.WorkingTimeApplication.dto;

import lombok.Data;

@Data
public class TaskRequest {
    private String title;
    private String description;
    private Long assignedToId;
}

