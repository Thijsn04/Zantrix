package com.zantrix.scheduling.web;

import com.zantrix.scheduling.SchedulingConflictException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
class SchedulingExceptionHandler {

    @ExceptionHandler(SchedulingConflictException.class)
    ProblemDetail conflict(SchedulingConflictException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "The practitioner or resource is already booked for this interval.");
        detail.setTitle("Scheduling conflict");
        detail.setType(URI.create("https://zantrix.org/problems/scheduling-conflict"));
        detail.setProperty("conflicts", exception.conflicts());
        return detail;
    }
}
