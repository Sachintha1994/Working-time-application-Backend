package com.thilina.WorkingTimeApplication.service.Impl;

import com.thilina.WorkingTimeApplication.enums.Role;
import com.thilina.WorkingTimeApplication.model.User;
import com.thilina.WorkingTimeApplication.repository.UserRepository;
import com.thilina.WorkingTimeApplication.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    @Override
    public List<User> getEngineersList() {
        return userRepository.findByRole(Role.ENGINEER);
    }
}
