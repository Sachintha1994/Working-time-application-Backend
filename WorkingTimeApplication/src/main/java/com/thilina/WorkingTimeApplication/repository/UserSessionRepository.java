package com.thilina.WorkingTimeApplication.repository;

import com.thilina.WorkingTimeApplication.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Integer> {
}
