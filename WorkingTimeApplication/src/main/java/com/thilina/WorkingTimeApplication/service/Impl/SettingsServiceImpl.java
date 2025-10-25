package com.thilina.WorkingTimeApplication.service.Impl;

import com.thilina.WorkingTimeApplication.dto.HolidayRequest;
import com.thilina.WorkingTimeApplication.dto.WorkingHoursRequest;
import com.thilina.WorkingTimeApplication.model.OneTimeHoliday;
import com.thilina.WorkingTimeApplication.model.RecurringHoliday;
import com.thilina.WorkingTimeApplication.model.WorkingHours;
import com.thilina.WorkingTimeApplication.repository.OneTimeHolidayRepository;
import com.thilina.WorkingTimeApplication.repository.RecurringHolidayRepository;
import com.thilina.WorkingTimeApplication.repository.WorkingHoursRepository;
import com.thilina.WorkingTimeApplication.service.SettingsService;
import com.thilina.WorkingTimeApplication.util.exception.DuplicateResourceException;
import com.thilina.WorkingTimeApplication.util.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SettingsServiceImpl implements SettingsService {

    private final WorkingHoursRepository workingHoursRepository;
    private final RecurringHolidayRepository recurringHolidayRepository;
    private final OneTimeHolidayRepository oneTimeHolidayRepository;

    @Override
    @Transactional
    public WorkingHours updateWorkingHours(WorkingHoursRequest request) {
        // Deactivate existing working hours
        workingHoursRepository.findByIsActiveTrue()
                .ifPresent(wh -> {
                    wh.setIsActive(false);
                    workingHoursRepository.save(wh);
                });

        // Create new working hours
        WorkingHours workingHours = new WorkingHours();
        workingHours.setStartTime(request.getStartTime());
        workingHours.setEndTime(request.getEndTime());
        workingHours.setIsActive(true);

        return workingHoursRepository.save(workingHours);
    }

    @Override
    public WorkingHours getWorkingHours() {
        return workingHoursRepository.findByIsActiveTrue()
                .orElseGet(() -> {
                    // Create default working hours if none exist
                    WorkingHours defaultHours = new WorkingHours();
                    defaultHours.setStartTime(LocalTime.of(8, 0));
                    defaultHours.setEndTime(LocalTime.of(16, 0));
                    defaultHours.setIsActive(true);
                    return workingHoursRepository.save(defaultHours);
                });
    }

    @Override
    @Transactional
    public RecurringHoliday addRecurringHoliday(HolidayRequest request) {
        RecurringHoliday holiday = new RecurringHoliday();
        holiday.setMonth(request.getMonth());
        holiday.setDay(request.getDay());
        holiday.setDescription(request.getDescription());

        return recurringHolidayRepository.save(holiday);
    }

    @Override
    public List<RecurringHoliday> getAllRecurringHolidays() {
        return recurringHolidayRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteRecurringHoliday(Long id) {
        recurringHolidayRepository.deleteById(id);
    }

    @Override
    @Transactional
    public OneTimeHoliday addOneTimeHoliday(HolidayRequest request) {
        if (oneTimeHolidayRepository.existsByDate(request.getDate())) {
            throw new DuplicateResourceException("Holiday already exists for this date");
        }

        OneTimeHoliday holiday = new OneTimeHoliday();
        holiday.setDate(request.getDate());
        holiday.setDescription(request.getDescription());

        return oneTimeHolidayRepository.save(holiday);
    }

    @Override
    public List<OneTimeHoliday> getAllOneTimeHolidays() {
        return oneTimeHolidayRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteOneTimeHoliday(Long id) {
        oneTimeHolidayRepository.deleteById(id);
    }

    @Override
    public RecurringHoliday getRecurringHolidayById(Long id) {
        return recurringHolidayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring holiday not found with id: " + id));
    }

    @Override
    public OneTimeHoliday getOneTimeHolidayById(Long id) {
        return oneTimeHolidayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("One-time holiday not found with id: " + id));
    }
}
