package com.thilina.WorkingTimeApplication.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EndDateCalculationRequest {
    private LocalDateTime startDateTime;
}
