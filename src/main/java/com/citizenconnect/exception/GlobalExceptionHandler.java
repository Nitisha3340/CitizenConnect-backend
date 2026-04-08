package com.citizenconnect.exception;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeExceptions(RuntimeException ex, HttpServletRequest request) {
        String message = ex.getMessage() == null ? "" : ex.getMessage();

        if ("Email already exists".equalsIgnoreCase(message)) {
            return buildResponse(
                    HttpStatus.CONFLICT,
                    "Conflict",
                    "A user with this email already exists.",
                    request.getRequestURI());
        }

        if ("Invalid password".equalsIgnoreCase(message) || "User not found".equalsIgnoreCase(message)) {
            return buildResponse(
                    HttpStatus.UNAUTHORIZED,
                    "Unauthorized",
                    "Invalid credentials.",
                    request.getRequestURI());
        }

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "Something went wrong. Please try again later.",
                request.getRequestURI());
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            HttpMessageNotReadableException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(Exception ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        if (ex instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
            for (FieldError fieldError : methodArgumentNotValidException.getBindingResult().getFieldErrors()) {
                fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        } else if (ex instanceof BindException bindException) {
            for (FieldError fieldError : bindException.getBindingResult().getFieldErrors()) {
                fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        }

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "Validation failed.",
                request.getRequestURI(),
                fieldErrors.isEmpty() ? null : fieldErrors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnknownException(Exception ex, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "Something went wrong. Please try again later.",
                request.getRequestURI());
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String error,
            String message,
            String path) {
        return buildResponse(status, error, message, path, null);
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String error,
            String message,
            String path,
            Map<String, String> errors) {
        ApiErrorResponse body = new ApiErrorResponse(
                OffsetDateTime.now().toString(),
                status.value(),
                error,
                message,
                path,
                errors);
        return ResponseEntity.status(status).body(body);
    }
}
