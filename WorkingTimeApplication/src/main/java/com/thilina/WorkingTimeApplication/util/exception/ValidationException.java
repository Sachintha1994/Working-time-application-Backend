package com.thilina.WorkingTimeApplication.util.exception;

import lombok.Getter;

@Getter
public class ValidationException extends BaseException {

    private static final long serialVersionUID = 4514782042622132336L;
    private final String code;
    private final String message;

    public ValidationException(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
