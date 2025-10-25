package com.thilina.WorkingTimeApplication.util.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class BaseResponseWrapper {
    private final String status;
    private final Integer statusCode;

    public BaseResponseWrapper(HttpStatus httpStatus) {
        this.status = httpStatus.name();
        this.statusCode = httpStatus.value();
    }
}
