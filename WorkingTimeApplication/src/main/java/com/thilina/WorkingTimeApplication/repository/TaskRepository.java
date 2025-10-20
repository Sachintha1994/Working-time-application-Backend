package com.thilina.WorkingTimeApplication.repository;

import com.thilina.WorkingTimeApplication.model.Task;
import com.thilina.WorkingTimeApplication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignedTo(User user);
    List<Task> findByCreatedBy(User user);
}
