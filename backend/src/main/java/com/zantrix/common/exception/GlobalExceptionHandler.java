package com.zantrix.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * Global exception handler for the Zantrix EPD application.
 * <p>
 * Catches application-wide exceptions and translates them into standardized HTTP responses.
 * This ensures that sensitive stack traces are not leaked to the frontend, maintaining
 * compliance with security and privacy requirements (such as NEN7510).
 * </p>
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles exceptions related to unauthorized access attempts.
     *
     * @param ex The AccessDeniedException thrown when a user lacks required permissions.
     * @return A standardized error response with HTTP 403 Forbidden.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Access Denied: You do not have permission to access this resource.");
    }

    /**
     * Handles exceptions related to invalid arguments passed to methods.
     *
     * @param ex The IllegalArgumentException containing the specific validation error.
     * @return A standardized error response with HTTP 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Handles exceptions caused by unreadable HTTP requests (e.g., malformed JSON).
     *
     * @param ex The HttpMessageNotReadableException.
     * @return A standardized error response with HTTP 400 Bad Request.
     */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadableException(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid input data format.");
    }

    /**
     * Fallback handler for all unexpected exceptions.
     *
     * @param ex The generic Exception caught.
     * @return A standardized error response with HTTP 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex) {
        // Prevent raw stack traces from bleeding to the frontend.
        // Log the exception securely here without PHI if necessary.
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal server error occurred.");
    }

    /**
     * Handles exceptions from invalid @Valid payloads.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Failed");
        body.put("message", "Input validation failed");
        body.put("fieldErrors", errors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Utility method to construct a consistent error response structure.
     *
     * @param status  The HTTP status corresponding to the error.
     * @param message A user-friendly error message.
     * @return The formatted ResponseEntity containing the error details.
     */
    private ResponseEntity<Object> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }
}
