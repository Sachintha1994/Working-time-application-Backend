package com.thilina.WorkingTimeApplication.service.impl;


import com.thilina.WorkingTimeApplication.model.WorkingHours;
import com.thilina.WorkingTimeApplication.repository.OneTimeHolidayRepository;
import com.thilina.WorkingTimeApplication.repository.RecurringHolidayRepository;
import com.thilina.WorkingTimeApplication.repository.WorkingHoursRepository;
import com.thilina.WorkingTimeApplication.util.exception.RequiredFieldException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeCalculationServiceImplTest {

    @Mock
    private WorkingHoursRepository workingHoursRepository;

    @Mock
    private RecurringHolidayRepository recurringHolidayRepository;

    @Mock
    private OneTimeHolidayRepository oneTimeHolidayRepository;

    @InjectMocks
    private TimeCalculationServiceImpl timeCalculationService;

    private WorkingHours workingHours;

    @BeforeEach
    void setUp() {
        workingHours = new WorkingHours();
        workingHours.setStartTime(LocalTime.of(9, 0));
        workingHours.setEndTime(LocalTime.of(17, 0));
        workingHours.setIsActive(true);
    }

    @Test
    void testCalculateEndDateTime_ThrowsException_WhenWorkingHoursNotConfigured() {
        when(workingHoursRepository.findByIsActiveTrue()).thenReturn(Optional.empty());

        LocalDateTime startDateTime = LocalDateTime.of(2024, 1, 15, 10, 0);

        assertThrows(RequiredFieldException.class, () ->
                timeCalculationService.calculateEndDateTime(startDateTime, 1.0));
    }

    @Test
    void testCalculateEndDateTime_ReturnsStartDateTime_WhenEstimateDaysIsZero() {
        when(workingHoursRepository.findByIsActiveTrue()).thenReturn(Optional.of(workingHours));

        LocalDateTime startDateTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime result = timeCalculationService.calculateEndDateTime(startDateTime, 0);

        assertEquals(startDateTime, result);
    }

    @Test
    void testCalculateEndDateTime_PositiveEstimate_WithinSameDay() {
        when(workingHoursRepository.findByIsActiveTrue()).thenReturn(Optional.of(workingHours));
        when(oneTimeHolidayRepository.existsByDate(any())).thenReturn(false);
        when(recurringHolidayRepository.findByMonthAndDay(anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        LocalDateTime startDateTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        double estimateDays = 0.5;

        LocalDateTime result = timeCalculationService.calculateEndDateTime(startDateTime, estimateDays);

        assertEquals(LocalDateTime.of(2024, 1, 15, 14, 0), result);
    }

    @Test
    void testCalculateEndDateTime_PositiveEstimate_SpansMultipleDays() {
        when(workingHoursRepository.findByIsActiveTrue()).thenReturn(Optional.of(workingHours));
        when(oneTimeHolidayRepository.existsByDate(any())).thenReturn(false);
        when(recurringHolidayRepository.findByMonthAndDay(anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        LocalDateTime startDateTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        double estimateDays = 2.0;

        LocalDateTime result = timeCalculationService.calculateEndDateTime(startDateTime, estimateDays);

        assertEquals(LocalDateTime.of(2024, 1, 17, 10, 0), result);
    }

    @Test
    void testCalculateEndDateTime_NegativeEstimate_WithinSameDay() {
        when(workingHoursRepository.findByIsActiveTrue()).thenReturn(Optional.of(workingHours));
        when(oneTimeHolidayRepository.existsByDate(any())).thenReturn(false);
        when(recurringHolidayRepository.findByMonthAndDay(anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        LocalDateTime startDateTime = LocalDateTime.of(2024, 1, 15, 14, 0);
        double estimateDays = -0.5;

        LocalDateTime result = timeCalculationService.calculateEndDateTime(startDateTime, estimateDays);

        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), result);
    }

    @Test
    void testCalculateEndDateTime_NegativeEstimate_SpansMultipleDays() {
        when(workingHoursRepository.findByIsActiveTrue()).thenReturn(Optional.of(workingHours));
        when(oneTimeHolidayRepository.existsByDate(any())).thenReturn(false);
        when(recurringHolidayRepository.findByMonthAndDay(anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        LocalDateTime startDateTime = LocalDateTime.of(2024, 1, 17, 14, 0);
        double estimateDays = -2.0;

        LocalDateTime result = timeCalculationService.calculateEndDateTime(startDateTime, estimateDays);

        assertEquals(LocalDateTime.of(2024, 1, 15, 14, 0), result);
    }

    @Test
    void testCalculateEndDateTime_SkipsWeekend() {
        when(workingHoursRepository.findByIsActiveTrue()).thenReturn(Optional.of(workingHours));
        when(oneTimeHolidayRepository.existsByDate(any())).thenReturn(false);
        when(recurringHolidayRepository.findByMonthAndDay(anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        LocalDateTime startDateTime = LocalDateTime.of(2024, 1, 19, 10, 0);
        double estimateDays = 1.5;

        LocalDateTime result = timeCalculationService.calculateEndDateTime(startDateTime, estimateDays);

        assertEquals(LocalDateTime.of(2024, 1, 22, 14, 0), result);
    }

    @Test
    void testCalculateEndDateTime_SkipsHoliday() {
        when(workingHoursRepository.findByIsActiveTrue()).thenReturn(Optional.of(workingHours));

        when(oneTimeHolidayRepository.existsByDate(LocalDate.of(2024, 1, 16)))
                .thenReturn(true);

        when(oneTimeHolidayRepository.existsByDate(argThat(date ->
                !date.equals(LocalDate.of(2024, 1, 16)))))
                .thenReturn(false);

        when(recurringHolidayRepository.findByMonthAndDay(anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        LocalDateTime startDateTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        double estimateDays = 1.5;

        LocalDateTime result = timeCalculationService.calculateEndDateTime(startDateTime, estimateDays);

        assertEquals(LocalDateTime.of(2024, 1, 17, 14, 0), result);
    }

    @Test
    void testCalculateEndDateTime_StartBeforeWorkingHours() {
        when(workingHoursRepository.findByIsActiveTrue()).thenReturn(Optional.of(workingHours));
        when(oneTimeHolidayRepository.existsByDate(any())).thenReturn(false);
        when(recurringHolidayRepository.findByMonthAndDay(anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        LocalDateTime startDateTime = LocalDateTime.of(2024, 1, 15, 8, 0); // Before 9 AM
        double estimateDays = 0.5;

        LocalDateTime result = timeCalculationService.calculateEndDateTime(startDateTime, estimateDays);

        assertEquals(LocalDateTime.of(2024, 1, 15, 13, 0), result);
    }

    @Test
    void testCalculateEndDateTime_StartAfterWorkingHours() {
        when(workingHoursRepository.findByIsActiveTrue()).thenReturn(Optional.of(workingHours));
        when(oneTimeHolidayRepository.existsByDate(any())).thenReturn(false);
        when(recurringHolidayRepository.findByMonthAndDay(anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        LocalDateTime startDateTime = LocalDateTime.of(2024, 1, 15, 18, 0);
        double estimateDays = 0.5;

        LocalDateTime result = timeCalculationService.calculateEndDateTime(startDateTime, estimateDays);

        assertEquals(LocalDateTime.of(2024, 1, 16, 9, 0), result);
    }

    @Test
    void testCalculateEndDateTime_StartOnWeekend() {
        when(workingHoursRepository.findByIsActiveTrue()).thenReturn(Optional.of(workingHours));
        when(oneTimeHolidayRepository.existsByDate(any())).thenReturn(false);
        when(recurringHolidayRepository.findByMonthAndDay(anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        LocalDateTime startDateTime = LocalDateTime.of(2024, 1, 20, 10, 0);
        double estimateDays = 0.5;

        LocalDateTime result = timeCalculationService.calculateEndDateTime(startDateTime, estimateDays);

        assertEquals(LocalDateTime.of(2024, 1, 22, 9, 0), result);
    }



    @Test
    void testIsWeekend_Saturday() {
        when(workingHoursRepository.findByIsActiveTrue()).thenReturn(Optional.of(workingHours));
        when(oneTimeHolidayRepository.existsByDate(any())).thenReturn(false);
        when(recurringHolidayRepository.findByMonthAndDay(anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        LocalDateTime saturday = LocalDateTime.of(2024, 1, 20, 10, 0);
        LocalDateTime result = timeCalculationService.calculateEndDateTime(saturday, 0.5);

        assertEquals(LocalDate.of(2024, 1, 22), result.toLocalDate());
    }

    @Test
    void testIsWeekend_Sunday() {
        when(workingHoursRepository.findByIsActiveTrue()).thenReturn(Optional.of(workingHours));
        when(oneTimeHolidayRepository.existsByDate(any())).thenReturn(false);
        when(recurringHolidayRepository.findByMonthAndDay(anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        LocalDateTime sunday = LocalDateTime.of(2024, 1, 21, 10, 0);
        LocalDateTime result = timeCalculationService.calculateEndDateTime(sunday, 0.5);

        assertEquals(LocalDate.of(2024, 1, 22), result.toLocalDate());
    }

    @Test
    void testIsHoliday_OneTimeHoliday() {
        when(workingHoursRepository.findByIsActiveTrue()).thenReturn(Optional.of(workingHours));
        when(oneTimeHolidayRepository.existsByDate(LocalDate.of(2024, 1, 15)))
                .thenReturn(true);
        when(recurringHolidayRepository.findByMonthAndDay(anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        LocalDateTime holiday = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime result = timeCalculationService.calculateEndDateTime(holiday, 0.5);

        assertNotEquals(LocalDate.of(2024, 1, 15), result.toLocalDate());
    }

    @Test
    void testIsHoliday_RecurringHoliday() {
        when(workingHoursRepository.findByIsActiveTrue()).thenReturn(Optional.of(workingHours));
        when(oneTimeHolidayRepository.existsByDate(any())).thenReturn(false);
        when(recurringHolidayRepository.findByMonthAndDay(anyInt(), anyInt()))
                .thenReturn(Collections.emptyList()); // No recurring holidays

        LocalDateTime startDateTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        double estimateDays = 0.5;

        LocalDateTime result = timeCalculationService.calculateEndDateTime(startDateTime, estimateDays);

        assertEquals(LocalDate.of(2024, 1, 15), result.toLocalDate());
    }

    @Test
    void testGetNextWorkingMoment_ThrowsException_WhenNoWorkingDaysInNextYear() {
        when(workingHoursRepository.findByIsActiveTrue()).thenReturn(Optional.of(workingHours));

        when(oneTimeHolidayRepository.existsByDate(any())).thenReturn(true);

        LocalDateTime startDateTime = LocalDateTime.of(2024, 1, 15, 18, 0);

        assertThrows(RuntimeException.class, () ->
                timeCalculationService.calculateEndDateTime(startDateTime, 0.5));
    }

    @Test
    void testGetPreviousWorkingMoment_ThrowsException_WhenNoWorkingDaysInPreviousYear() {
        when(workingHoursRepository.findByIsActiveTrue()).thenReturn(Optional.of(workingHours));

        when(oneTimeHolidayRepository.existsByDate(any())).thenReturn(true);

        LocalDateTime startDateTime = LocalDateTime.of(2024, 1, 15, 8, 0);

        assertThrows(RuntimeException.class, () ->
                timeCalculationService.calculateEndDateTime(startDateTime, -0.5));
    }

    @Test
    void testCalculateEndDateTime_BackwardFromBeforeWorkingHours() {
        when(workingHoursRepository.findByIsActiveTrue()).thenReturn(Optional.of(workingHours));
        when(oneTimeHolidayRepository.existsByDate(any())).thenReturn(false);
        when(recurringHolidayRepository.findByMonthAndDay(anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        LocalDateTime startDateTime = LocalDateTime.of(2024, 1, 16, 8, 0);
        double estimateDays = -0.5;

        LocalDateTime result = timeCalculationService.calculateEndDateTime(startDateTime, estimateDays);

        assertEquals(LocalDateTime.of(2024, 1, 15, 17, 0), result);
    }

    @Test
    void testCalculateEndDateTime_ExactlyAtEndTime() {
        when(workingHoursRepository.findByIsActiveTrue()).thenReturn(Optional.of(workingHours));
        when(oneTimeHolidayRepository.existsByDate(any())).thenReturn(false);
        when(recurringHolidayRepository.findByMonthAndDay(anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        LocalDateTime startDateTime = LocalDateTime.of(2024, 1, 15, 17, 0);
        double estimateDays = 0.5;

        LocalDateTime result = timeCalculationService.calculateEndDateTime(startDateTime, estimateDays);

        assertEquals(LocalDateTime.of(2024, 1, 16, 13, 0), result);
    }

    @Test
    void testCalculateEndDateTime_ExactlyAtStartTime() {
        when(workingHoursRepository.findByIsActiveTrue()).thenReturn(Optional.of(workingHours));
        when(oneTimeHolidayRepository.existsByDate(any())).thenReturn(false);
        when(recurringHolidayRepository.findByMonthAndDay(anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        LocalDateTime startDateTime = LocalDateTime.of(2024, 1, 15, 9, 0);
        double estimateDays = 1.0;

        LocalDateTime result = timeCalculationService.calculateEndDateTime(startDateTime, estimateDays);

        assertEquals(LocalDateTime.of(2024, 1, 15, 17, 0), result);
    }
}
