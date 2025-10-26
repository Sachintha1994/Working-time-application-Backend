package com.thilina.WorkingTimeApplication.service.Impl;

import com.thilina.WorkingTimeApplication.model.RecurringHoliday;
import com.thilina.WorkingTimeApplication.model.WorkingHours;
import com.thilina.WorkingTimeApplication.repository.OneTimeHolidayRepository;
import com.thilina.WorkingTimeApplication.repository.RecurringHolidayRepository;
import com.thilina.WorkingTimeApplication.repository.WorkingHoursRepository;
import com.thilina.WorkingTimeApplication.service.TimeCalculationService;
import com.thilina.WorkingTimeApplication.util.exception.RequiredFieldException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeCalculationServiceImpl implements TimeCalculationService {

    private final WorkingHoursRepository workingHoursRepository;
    private final RecurringHolidayRepository recurringHolidayRepository;
    private final OneTimeHolidayRepository oneTimeHolidayRepository;

    @Override
    public LocalDateTime calculateEndDateTime(LocalDateTime startDateTime, double estimateDays) {
        log.info("=== Starting End Date Calculation ===");
        log.info("Start DateTime: {}, Estimate Days: {}", startDateTime, estimateDays);

        WorkingHours workingHours = workingHoursRepository.findByIsActiveTrue()
                .orElseThrow(() -> new RequiredFieldException("Working hours not configured"));

        log.info("Working Hours - Start: {}, End: {}", workingHours.getStartTime(), workingHours.getEndTime());

        if (estimateDays == 0) {
            log.info("Estimate is 0, returning start date time");
            return startDateTime;
        }

        boolean isNegative = estimateDays < 0;
        double absEstimate = Math.abs(estimateDays);
        double workingHoursPerDay = getWorkingHoursPerDay(workingHours);

        log.info("Is Negative: {}, Abs Estimate: {}, Working Hours Per Day: {}",
                isNegative, absEstimate, workingHoursPerDay);

        LocalDateTime currentDateTime = startDateTime;
        double remainingHours = absEstimate * workingHoursPerDay;

        log.info("Initial Remaining Hours: {}", remainingHours);

        int iterationCount = 0;
        int maxIterations = 10000; // Safety limit

        while (remainingHours > 0.0001) { // Small threshold for floating point
            iterationCount++;

            if (iterationCount > maxIterations) {
                log.error("INFINITE LOOP DETECTED! Breaking after {} iterations", maxIterations);
                log.error("Current DateTime: {}, Remaining Hours: {}", currentDateTime, remainingHours);
                throw new RuntimeException("End date calculation exceeded maximum iterations");
            }

            if (iterationCount % 100 == 0) {
                log.warn("Iteration {}: Current={}, Remaining={}", iterationCount, currentDateTime, remainingHours);
            }

            log.debug("Iteration {}: Current DateTime: {}, Remaining Hours: {}",
                    iterationCount, currentDateTime, remainingHours);

            LocalDateTime beforeMove = currentDateTime;

            if (isNegative) {
                currentDateTime = moveBackward(currentDateTime, workingHours, remainingHours);
            } else {
                currentDateTime = moveForward(currentDateTime, workingHours, remainingHours);
            }

            log.debug("After move - Before: {}, After: {}", beforeMove, currentDateTime);

            // Calculate actual hours processed in this iteration
            double hoursProcessed;
            if (currentDateTime.equals(beforeMove)) {
                // We didn't move, need to skip to next/previous working day
                log.debug("No movement detected, jumping to next/previous working moment");
                currentDateTime = isNegative
                        ? getPreviousWorkingMoment(currentDateTime, workingHours)
                        : getNextWorkingMoment(currentDateTime, workingHours);
                hoursProcessed = 0; // No hours consumed by the jump
            } else {
                // Calculate hours between before and after positions
                long minutesDiff = isNegative
                        ? Duration.between(currentDateTime, beforeMove).toMinutes()
                        : Duration.between(beforeMove, currentDateTime).toMinutes();
                hoursProcessed = minutesDiff / 60.0;
            }

            log.debug("Hours Processed: {}", hoursProcessed);

            remainingHours -= hoursProcessed;

            log.debug("Remaining Hours after deduction: {}", remainingHours);
        }

        log.info("=== Calculation Complete ===");
        log.info("Final DateTime: {}, Total Iterations: {}", currentDateTime, iterationCount);

        return currentDateTime;
    }

    private LocalDateTime moveForward(LocalDateTime current, WorkingHours wh, double hours) {
        log.debug("moveForward - Current: {}, Hours to add: {}", current, hours);

        LocalDate currentDate = current.toLocalDate();
        LocalTime currentTime = current.toLocalTime();

        // Skip to next working moment if currently outside working hours
        if (currentTime.isBefore(wh.getStartTime())) {
            log.debug("Current time {} is before start time {}, adjusting to start time",
                    currentTime, wh.getStartTime());
            currentTime = wh.getStartTime();
            current = LocalDateTime.of(currentDate, currentTime);
        }

        if (currentTime.isAfter(wh.getEndTime()) || isWeekend(currentDate) || isHoliday(currentDate)) {
            log.debug("Outside working hours/weekend/holiday, jumping to next working moment");
            return getNextWorkingMoment(current, wh);
        }

        // Calculate available hours today
        double availableHours = Duration.between(currentTime, wh.getEndTime()).toMinutes() / 60.0;
        log.debug("Available hours from {} to {}: {}", currentTime, wh.getEndTime(), availableHours);

        if (hours <= availableHours) {
            LocalDateTime result = current.plusMinutes((long)(hours * 60));
            log.debug("Adding {} hours, result: {}", hours, result);
            return result;
        }

        // Move to end of working day
        LocalDateTime result = LocalDateTime.of(currentDate, wh.getEndTime());
        log.debug("Not enough hours today, moving to end of day: {}", result);
        return result;
    }

    private LocalDateTime moveBackward(LocalDateTime current, WorkingHours wh, double hours) {
        log.debug("moveBackward - Current: {}, Hours to subtract: {}", current, hours);

        LocalDate currentDate = current.toLocalDate();
        LocalTime currentTime = current.toLocalTime();

        // Skip to previous working moment if currently outside working hours
        if (currentTime.isAfter(wh.getEndTime())) {
            log.debug("Current time {} is after end time {}, adjusting to end time",
                    currentTime, wh.getEndTime());
            currentTime = wh.getEndTime();
            current = LocalDateTime.of(currentDate, currentTime);
        }

        if (currentTime.isBefore(wh.getStartTime()) || isWeekend(currentDate) || isHoliday(currentDate)) {
            log.debug("Outside working hours/weekend/holiday, jumping to previous working moment");
            return getPreviousWorkingMoment(current, wh);
        }

        // Calculate available hours from start of day to current time
        double availableHours = Duration.between(wh.getStartTime(), currentTime).toMinutes() / 60.0;
        log.debug("Available hours from {} to {}: {}", wh.getStartTime(), currentTime, availableHours);

        if (hours <= availableHours) {
            LocalDateTime result = current.minusMinutes((long)(hours * 60));
            log.debug("Subtracting {} hours, result: {}", hours, result);
            return result;
        }

        // Move to start of working day
        LocalDateTime result = LocalDateTime.of(currentDate, wh.getStartTime());
        log.debug("Not enough hours today, moving to start of day: {}", result);
        return result;
    }

    private LocalDateTime getNextWorkingMoment(LocalDateTime current, WorkingHours wh) {
        LocalDate date = current.toLocalDate().plusDays(1);
        log.debug("Getting next working moment from {}", current);

        int daysSkipped = 0;
        while (isWeekend(date) || isHoliday(date)) {
            daysSkipped++;
            date = date.plusDays(1);
            if (daysSkipped > 365) {
                log.error("Skipped over 365 days looking for working day!");
                throw new RuntimeException("No working days found in next year");
            }
        }

        if (daysSkipped > 0) {
            log.debug("Skipped {} non-working days", daysSkipped);
        }

        LocalDateTime result = LocalDateTime.of(date, wh.getStartTime());
        log.debug("Next working moment: {}", result);
        return result;
    }

    private LocalDateTime getPreviousWorkingMoment(LocalDateTime current, WorkingHours wh) {
        LocalDate date = current.toLocalDate().minusDays(1);
        log.debug("Getting previous working moment from {}", current);

        int daysSkipped = 0;
        while (isWeekend(date) || isHoliday(date)) {
            daysSkipped++;
            date = date.minusDays(1);
            if (daysSkipped > 365) {
                log.error("Skipped over 365 days looking for working day!");
                throw new RuntimeException("No working days found in previous year");
            }
        }

        if (daysSkipped > 0) {
            log.debug("Skipped {} non-working days", daysSkipped);
        }

        LocalDateTime result = LocalDateTime.of(date, wh.getEndTime());
        log.debug("Previous working moment: {}", result);
        return result;
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    private boolean isHoliday(LocalDate date) {
        // Check one-time holiday
        if (oneTimeHolidayRepository.existsByDate(date)) {
            return true;
        }

        // Check recurring holiday
        List<RecurringHoliday> recurring = recurringHolidayRepository
                .findByMonthAndDay(date.getMonthValue(), date.getDayOfMonth());
        return !recurring.isEmpty();
    }

    private double getWorkingHoursPerDay(WorkingHours wh) {
        return Duration.between(wh.getStartTime(), wh.getEndTime()).toMinutes() / 60.0;
    }
}
