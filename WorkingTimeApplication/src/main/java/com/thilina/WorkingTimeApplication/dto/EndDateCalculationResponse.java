package com.thilina.WorkingTimeApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class EndDateCalculationResponse {
    private LocalDateTime endDateTime;
}
