package com.thilina.WorkingTimeApplication.service;

import java.time.LocalDateTime;

public interface TimeCalculationService {
    LocalDateTime calculateEndDateTime(LocalDateTime startDateTime, double estimateDays);

}
