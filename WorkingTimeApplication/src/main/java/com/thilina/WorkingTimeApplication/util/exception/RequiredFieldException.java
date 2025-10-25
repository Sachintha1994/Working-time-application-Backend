package com.thilina.WorkingTimeApplication.util.exception;

import lombok.Getter;

@Getter
public class RequiredFieldException extends RuntimeException {

    private final String code;

    public RequiredFieldException(String code, String message) {
        super(message);
        this.code = code;
    }

    public RequiredFieldException(String fieldName) {
        super(fieldName + " is required");
        this.code = "REQUIRED_FIELD_MISSING";
    }
}
