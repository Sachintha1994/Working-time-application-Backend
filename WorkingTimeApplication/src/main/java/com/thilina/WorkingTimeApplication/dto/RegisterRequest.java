package com.thilina.WorkingTimeApplication.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNo;
    private String username;
    private String password;
}
