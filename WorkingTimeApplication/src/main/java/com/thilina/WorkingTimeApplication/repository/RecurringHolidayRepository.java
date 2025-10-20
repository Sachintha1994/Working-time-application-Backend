package com.thilina.WorkingTimeApplication.repository;

import com.thilina.WorkingTimeApplication.model.RecurringHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecurringHolidayRepository extends JpaRepository<RecurringHoliday, Long> {
    List<RecurringHoliday> findByMonthAndDay(Integer month, Integer day);
}
