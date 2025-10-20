package com.thilina.WorkingTimeApplication.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class HolidayRequest {
    private Integer month;
    private Integer day;
    private LocalDate date;
    private String description;
}
