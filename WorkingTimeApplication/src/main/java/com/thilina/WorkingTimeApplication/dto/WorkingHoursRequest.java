package com.thilina.WorkingTimeApplication.dto;

import lombok.Data;

import java.time.LocalTime;

@Data
public class WorkingHoursRequest {
    private LocalTime startTime;
    private LocalTime endTime;
}
