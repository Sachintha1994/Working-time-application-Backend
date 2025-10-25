package com.thilina.WorkingTimeApplication.service.Impl;

import com.thilina.WorkingTimeApplication.model.RecurringHoliday;
import com.thilina.WorkingTimeApplication.model.WorkingHours;
import com.thilina.WorkingTimeApplication.repository.OneTimeHolidayRepository;
import com.thilina.WorkingTimeApplication.repository.RecurringHolidayRepository;
import com.thilina.WorkingTimeApplication.repository.WorkingHoursRepository;
import com.thilina.WorkingTimeApplication.service.TimeCalculationService;
import com.thilina.WorkingTimeApplication.util.exception.RequiredFieldException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimeCalculationServiceImpl implements TimeCalculationService {

    private final WorkingHoursRepository workingHoursRepository;
    private final RecurringHolidayRepository recurringHolidayRepository;
    private final OneTimeHolidayRepository oneTimeHolidayRepository;

    @Override
    public LocalDateTime calculateEndDateTime(LocalDateTime startDateTime, double estimateDays) {
        WorkingHours workingHours = workingHoursRepository.findByIsActiveTrue()
                .orElseThrow(() -> new RequiredFieldException("Working hours not configured"));

        if (estimateDays == 0) {
            return startDateTime;
        }

        boolean isNegative = estimateDays < 0;
        double absEstimate = Math.abs(estimateDays);

        LocalDateTime currentDateTime = startDateTime;
        double remainingHours = absEstimate * getWorkingHoursPerDay(workingHours);

        while (remainingHours > 0.0001) { // Small threshold for floating point
            if (isNegative) {
                currentDateTime = moveBackward(currentDateTime, workingHours, remainingHours);
            } else {
                currentDateTime = moveForward(currentDateTime, workingHours, remainingHours);
            }

            double hoursProcessed = calculateHoursProcessed(currentDateTime, workingHours, isNegative);
            remainingHours -= hoursProcessed;

            if (remainingHours > 0.0001) {
                currentDateTime = isNegative
                        ? getPreviousWorkingMoment(currentDateTime, workingHours)
                        : getNextWorkingMoment(currentDateTime, workingHours);
            }
        }

        return currentDateTime;
    }

    private LocalDateTime moveForward(LocalDateTime current, WorkingHours wh, double hours) {
        LocalDate currentDate = current.toLocalDate();
        LocalTime currentTime = current.toLocalTime();

        // Skip to next working moment if currently outside working hours
        if (currentTime.isBefore(wh.getStartTime())) {
            currentTime = wh.getStartTime();
        } else if (currentTime.isAfter(wh.getEndTime()) ||
                isWeekend(currentDate) ||
                isHoliday(currentDate)) {
            return getNextWorkingMoment(current, wh);
        }

        // Calculate available hours today
        double availableHours = Duration.between(currentTime, wh.getEndTime()).toMinutes() / 60.0;

        if (hours <= availableHours) {
            return current.plusMinutes((long)(hours * 60));
        }

        return current;
    }

    private LocalDateTime moveBackward(LocalDateTime current, WorkingHours wh, double hours) {
        LocalDate currentDate = current.toLocalDate();
        LocalTime currentTime = current.toLocalTime();

        // Skip to previous working moment if currently outside working hours
        if (currentTime.isAfter(wh.getEndTime())) {
            currentTime = wh.getEndTime();
        } else if (currentTime.isBefore(wh.getStartTime()) ||
                isWeekend(currentDate) ||
                isHoliday(currentDate)) {
            return getPreviousWorkingMoment(current, wh);
        }

        // Calculate available hours from start of day to current time
        double availableHours = Duration.between(wh.getStartTime(), currentTime).toMinutes() / 60.0;

        if (hours <= availableHours) {
            return current.minusMinutes((long)(hours * 60));
        }

        return current;
    }

    private double calculateHoursProcessed(LocalDateTime current, WorkingHours wh, boolean backward) {
        LocalTime time = current.toLocalTime();

        if (backward) {
            return Duration.between(wh.getStartTime(), time).toMinutes() / 60.0;
        } else {
            return Duration.between(time, wh.getEndTime()).toMinutes() / 60.0;
        }
    }

    private LocalDateTime getNextWorkingMoment(LocalDateTime current, WorkingHours wh) {
        LocalDate date = current.toLocalDate().plusDays(1);

        while (isWeekend(date) || isHoliday(date)) {
            date = date.plusDays(1);
        }

        return LocalDateTime.of(date, wh.getStartTime());
    }

    private LocalDateTime getPreviousWorkingMoment(LocalDateTime current, WorkingHours wh) {
        LocalDate date = current.toLocalDate().minusDays(1);

        while (isWeekend(date) || isHoliday(date)) {
            date = date.minusDays(1);
        }

        return LocalDateTime.of(date, wh.getEndTime());
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
