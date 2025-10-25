package com.thilina.WorkingTimeApplication.util.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class FailedResponseWrapper extends BaseResponseWrapper {
    private final ValidationFailure validationFailure;

    public FailedResponseWrapper(String code, String message) {
        super(HttpStatus.EXPECTATION_FAILED);
        this.validationFailure = new ValidationFailure(code, message);
    }


    @Getter
    public static class ValidationFailure {
        private final String code;
        private final String message;

        public ValidationFailure(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}