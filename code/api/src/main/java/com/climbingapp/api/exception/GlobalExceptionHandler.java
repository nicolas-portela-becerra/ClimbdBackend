package com.climbingapp.api.exception;

import com.climbingapp.api.dto.ErrorResponse;
import com.climbingapp.domain.exception.NotFoundException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleUnauthorizedException() {
        log.warn("Failed authentication attempt");
        return new ResponseEntity<>(
                new ErrorResponse()
                        .message("Invalid credentials")
                        .status(HttpStatus.UNAUTHORIZED.value()),
                HttpStatus.UNAUTHORIZED);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({DisabledException.class})
    public ResponseEntity<ErrorResponse> handleDisabledException() {
        log.warn("Authentication attempt on a deactivated account");
        return new ResponseEntity<>(
                new ErrorResponse()
                        .message("User account is deactivated")
                        .status(HttpStatus.UNAUTHORIZED.value()),
                HttpStatus.UNAUTHORIZED);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorResponse> handleBadRequestException() {
        return new ResponseEntity<>(
                new ErrorResponse()
                        .message("Invalid request parameters")
                        .status(HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({DataIntegrityViolationException.class})
    public ResponseEntity<ErrorResponse> handleConflictException() {
        return new ResponseEntity<>(
                new ErrorResponse()
                        .message(
                                "Request could not be processed due to conflict in the current state of the resource")
                        .status(HttpStatus.CONFLICT.value()),
                HttpStatus.CONFLICT);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> handleForbiddenException() {
        return new ResponseEntity<>(
                new ErrorResponse()
                        .message("User does not have permission to access this resource")
                        .status(HttpStatus.FORBIDDEN.value()),
                HttpStatus.FORBIDDEN);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
        return new ResponseEntity<>(
                new ErrorResponse().message(ex.getMessage()).status(HttpStatus.NOT_FOUND.value()),
                HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException e) {
        log.warn("Bad request: {}", e.getMessage());
        return new ResponseEntity<>(
                new ErrorResponse().message(e.getMessage()).status(HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST);
    }
}
