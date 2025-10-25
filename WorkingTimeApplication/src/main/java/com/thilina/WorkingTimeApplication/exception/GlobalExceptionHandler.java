package com.thilina.WorkingTimeApplication.exception;

import com.thilina.WorkingTimeApplication.util.exception.*;
import com.thilina.WorkingTimeApplication.util.response.FailedResponseWrapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ValidationException.class})
    public ResponseEntity<FailedResponseWrapper> handleValidationException(ValidationException ex) {
        return new ResponseEntity<>(new FailedResponseWrapper(ex.getCode(), ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ServerErrorException.class})
    public ResponseEntity<FailedResponseWrapper> handleServerException(ServerErrorException ex) {
        return new ResponseEntity<>(new FailedResponseWrapper(ex.getCode(), ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<FailedResponseWrapper> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return new ResponseEntity<>(new FailedResponseWrapper(ex.getCode(), ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<FailedResponseWrapper> handleDuplicateResourceException(DuplicateResourceException ex) {
        return new ResponseEntity<>(new FailedResponseWrapper(ex.getCode(), ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<FailedResponseWrapper> handleAccessDeniedException(AccessDeniedException ex) {
        return new ResponseEntity<>(new FailedResponseWrapper("ACCESS_DENIED", ex.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<FailedResponseWrapper> handleIllegalArgumentException(IllegalArgumentException ex) {
        return new ResponseEntity<>(new FailedResponseWrapper("INVALID_ARGUMENT", ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<FailedResponseWrapper> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String errorMessage = errors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));
        return new ResponseEntity<>(new FailedResponseWrapper("VALIDATION_ERROR", errorMessage), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<FailedResponseWrapper> handleAuthenticationException(AuthenticationException ex) {
        return new ResponseEntity<>(new FailedResponseWrapper("AUTHENTICATION_FAILED", "Authentication failed"), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<FailedResponseWrapper> handleBadCredentialsException(BadCredentialsException ex) {
        return new ResponseEntity<>(new FailedResponseWrapper("BAD_CREDENTIALS", ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(RequiredFieldException.class)
    public ResponseEntity<FailedResponseWrapper> handleRequiredFieldException(RequiredFieldException ex) {
        return new ResponseEntity<>(new FailedResponseWrapper(ex.getCode(), ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
