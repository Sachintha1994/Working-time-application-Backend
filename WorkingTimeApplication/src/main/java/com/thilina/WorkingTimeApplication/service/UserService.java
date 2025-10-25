package com.thilina.WorkingTimeApplication.service;

import com.thilina.WorkingTimeApplication.dto.UserResponse;
import com.thilina.WorkingTimeApplication.model.User;

import java.util.List;

public interface UserService {
    User getUserById(Long id);
    User getUserByUsername(String username);
    List<UserResponse> getAllEngineers();
}
