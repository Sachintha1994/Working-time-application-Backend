package com.thilina.WorkingTimeApplication.service.impl;

import com.thilina.WorkingTimeApplication.dto.UserResponse;
import com.thilina.WorkingTimeApplication.enums.Role;
import com.thilina.WorkingTimeApplication.model.User;
import com.thilina.WorkingTimeApplication.repository.UserRepository;
import com.thilina.WorkingTimeApplication.service.UserService;
import com.thilina.WorkingTimeApplication.util.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }


    @Override
    public List<UserResponse> getAllEngineers() {
        log.info("Fetching all users with role ENGINEER");

        List<User> engineers = userRepository.findByRole(Role.ENGINEER);

        if (engineers.isEmpty()) {
            log.warn("No engineers found in the system");
            return Collections.emptyList();
        }

        return engineers.stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getPhoneNo(),
                        user.getRole().name()
                ))
                .toList();
    }

}
