package com.thilina.WorkingTimeApplication.controller;

import com.thilina.WorkingTimeApplication.dto.HolidayRequest;
import com.thilina.WorkingTimeApplication.dto.WorkingHoursRequest;
import com.thilina.WorkingTimeApplication.model.OneTimeHoliday;
import com.thilina.WorkingTimeApplication.model.RecurringHoliday;
import com.thilina.WorkingTimeApplication.model.WorkingHours;
import com.thilina.WorkingTimeApplication.service.SettingsService;
import com.thilina.WorkingTimeApplication.util.response.SuccessResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settings")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@PreAuthorize("hasRole('PROJECT_MANAGER')")
public class SettingsController {

    private final SettingsService settingsService;

    // ========================================================================
    // WORKING HOURS ENDPOINTS
    // ========================================================================

    /**
     * Update working hours
     * PUT /api/settings/working-hours
     *
     * Request Body:
     * {
     *   "startTime": "09:00:00",
     *   "endTime": "17:00:00"
     * }
     */
    // ========================================================================
    // WORKING HOURS ENDPOINTS
    // ========================================================================

    /**
     * Update working hours configuration
     * PUT /api/settings/working-hours
     */
    @PutMapping("/working-hours")
    public ResponseEntity<SuccessResponseWrapper<WorkingHours>> updateWorkingHours(
            @Validated @RequestBody WorkingHoursRequest request) {

        WorkingHours workingHours = settingsService.updateWorkingHours(request);
        return new ResponseEntity<>(new SuccessResponseWrapper<>(workingHours), HttpStatus.ACCEPTED);
    }

    /**
     * Get current working hours configuration
     * GET /api/settings/working-hours
     */
    @GetMapping("/working-hours")
    public ResponseEntity<SuccessResponseWrapper<WorkingHours>> getWorkingHours() {
        WorkingHours workingHours = settingsService.getWorkingHours();
        return new ResponseEntity<>(new SuccessResponseWrapper<>(workingHours), HttpStatus.OK);
    }

    // ========================================================================
    // RECURRING HOLIDAYS ENDPOINTS
    // ========================================================================

    /**
     * Add a recurring holiday (e.g., every May 17)
     * POST /api/settings/recurring-holidays
     *
     * Request Body:
     * {
     *   "month": 5,
     *   "day": 17,
     *   "description": "Annual Holiday"
     * }
     */
    @PostMapping("/recurring-holidays")
    public ResponseEntity<SuccessResponseWrapper<RecurringHoliday>> addRecurringHoliday(
            @Validated @RequestBody HolidayRequest request) {

        RecurringHoliday holiday = settingsService.addRecurringHoliday(request);
        return new ResponseEntity<>(new SuccessResponseWrapper<>(holiday), HttpStatus.CREATED);
    }

    /**
     * Get all recurring holidays
     * GET /api/settings/recurring-holidays
     */
    @GetMapping("/recurring-holidays")
    public ResponseEntity<SuccessResponseWrapper<List<RecurringHoliday>>> getAllRecurringHolidays() {
        List<RecurringHoliday> holidays = settingsService.getAllRecurringHolidays();
        return new ResponseEntity<>(new SuccessResponseWrapper<>(holidays), HttpStatus.OK);
    }

    /**
     * Get a specific recurring holiday by ID
     * GET /api/settings/recurring-holidays/{id}
     */
    @GetMapping("/recurring-holidays/{id}")
    public ResponseEntity<SuccessResponseWrapper<RecurringHoliday>> getRecurringHolidayById(
            @PathVariable Long id) {
        RecurringHoliday holiday = settingsService.getRecurringHolidayById(id);
        return new ResponseEntity<>(new SuccessResponseWrapper<>(holiday), HttpStatus.OK);
    }

    /**
     * Delete a recurring holiday
     * DELETE /api/settings/recurring-holidays/{id}
     */
    @DeleteMapping("/recurring-holidays/{id}")
    public ResponseEntity<SuccessResponseWrapper<String>> deleteRecurringHoliday(
            @PathVariable Long id) {
        settingsService.deleteRecurringHoliday(id);
        return new ResponseEntity<>(new SuccessResponseWrapper<>("Recurring holiday deleted successfully"), HttpStatus.ACCEPTED);
    }

    // ========================================================================
    // ONE-TIME HOLIDAYS ENDPOINTS
    // ========================================================================

    /**
     * Add a one-time holiday (e.g., May 27, 2004)
     * POST /api/settings/one-time-holidays
     *
     * Request Body:
     * {
     *   "date": "2004-05-27",
     *   "description": "Special Holiday"
     * }
     */
    @PostMapping("/one-time-holidays")
    public ResponseEntity<SuccessResponseWrapper<OneTimeHoliday>> addOneTimeHoliday(
            @Validated @RequestBody HolidayRequest request) {

        OneTimeHoliday holiday = settingsService.addOneTimeHoliday(request);
        return new ResponseEntity<>(new SuccessResponseWrapper<>(holiday), HttpStatus.CREATED);
    }

    /**
     * Get all one-time holidays
     * GET /api/settings/one-time-holidays
     */
    @GetMapping("/one-time-holidays")
    public ResponseEntity<SuccessResponseWrapper<List<OneTimeHoliday>>> getAllOneTimeHolidays() {
        List<OneTimeHoliday> holidays = settingsService.getAllOneTimeHolidays();
        return new ResponseEntity<>(new SuccessResponseWrapper<>(holidays), HttpStatus.OK);
    }

    /**
     * Get a specific one-time holiday by ID
     * GET /api/settings/one-time-holidays/{id}
     */
    @GetMapping("/one-time-holidays/{id}")
    public ResponseEntity<SuccessResponseWrapper<OneTimeHoliday>> getOneTimeHolidayById(
            @PathVariable Long id) {
        OneTimeHoliday holiday = settingsService.getOneTimeHolidayById(id);
        return new ResponseEntity<>(new SuccessResponseWrapper<>(holiday), HttpStatus.OK);
    }

    /**
     * Delete a one-time holiday
     * DELETE /api/settings/one-time-holidays/{id}
     */
    @DeleteMapping("/one-time-holidays/{id}")
    public ResponseEntity<SuccessResponseWrapper<String>> deleteOneTimeHoliday(
            @PathVariable Long id) {
        settingsService.deleteOneTimeHoliday(id);
        return new ResponseEntity<>(new SuccessResponseWrapper<>("One-time holiday deleted successfully"), HttpStatus.ACCEPTED);
    }

    /**
     * Bulk delete one-time holidays
     * DELETE /api/settings/one-time-holidays
     */
    @DeleteMapping("/one-time-holidays")
    public ResponseEntity<SuccessResponseWrapper<String>> bulkDeleteOneTimeHolidays(
            @RequestBody List<Long> ids) {
        ids.forEach(settingsService::deleteOneTimeHoliday);
        return new ResponseEntity<>(new SuccessResponseWrapper<>(ids.size() + " holidays deleted successfully"), HttpStatus.ACCEPTED);
    }
}

