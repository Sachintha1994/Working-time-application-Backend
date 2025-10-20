package com.thilina.WorkingTimeApplication.service;

import com.thilina.WorkingTimeApplication.dto.HolidayRequest;
import com.thilina.WorkingTimeApplication.dto.WorkingHoursRequest;
import com.thilina.WorkingTimeApplication.model.OneTimeHoliday;
import com.thilina.WorkingTimeApplication.model.RecurringHoliday;
import com.thilina.WorkingTimeApplication.model.WorkingHours;

import java.util.List;

public interface SettingsService {
    WorkingHours updateWorkingHours(WorkingHoursRequest request);
    WorkingHours getWorkingHours();
    RecurringHoliday addRecurringHoliday(HolidayRequest request);
    List<RecurringHoliday> getAllRecurringHolidays();
    void deleteRecurringHoliday(Long id);
    OneTimeHoliday addOneTimeHoliday(HolidayRequest request);
    List<OneTimeHoliday> getAllOneTimeHolidays();
    void deleteOneTimeHoliday(Long id);
}
