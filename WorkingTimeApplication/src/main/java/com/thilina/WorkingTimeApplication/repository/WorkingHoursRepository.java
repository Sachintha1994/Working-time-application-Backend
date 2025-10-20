package com.thilina.WorkingTimeApplication.repository;

import com.thilina.WorkingTimeApplication.model.WorkingHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkingHoursRepository extends JpaRepository<WorkingHours, Long> {
    Optional<WorkingHours> findByIsActiveTrue();
}
