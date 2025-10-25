package com.thilina.WorkingTimeApplication.util.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class SuccessResponseWrapper<T> extends BaseResponseWrapper {
    private final T content;

    public SuccessResponseWrapper(T content) {
        super(HttpStatus.OK);
        this.content = content;
    }
}
