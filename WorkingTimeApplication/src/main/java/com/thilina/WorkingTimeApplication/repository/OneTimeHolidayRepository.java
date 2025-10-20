package com.thilina.WorkingTimeApplication.repository;

import com.thilina.WorkingTimeApplication.model.OneTimeHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface OneTimeHolidayRepository extends JpaRepository<OneTimeHoliday, Long> {
    Optional<OneTimeHoliday> findByDate(LocalDate date);
    boolean existsByDate(LocalDate date);
}
