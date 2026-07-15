package com.zantrix.medications.web;

import com.zantrix.medications.MedicationSafetyException;
import com.zantrix.cds.MedicationCodeValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
class MedicationExceptionHandler {

    @ExceptionHandler(MedicationCodeValidationException.class)
    ProblemDetail code(MedicationCodeValidationException exception) {
        HttpStatus status = exception.unavailable() ? HttpStatus.SERVICE_UNAVAILABLE
                : HttpStatus.UNPROCESSABLE_ENTITY;
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());
        detail.setTitle(exception.unavailable() ? "RxNorm unavailable" : "Invalid RxNorm ingredient");
        detail.setType(URI.create("https://zantrix.org/problems/rxnorm-validation"));
        return detail;
    }

    @ExceptionHandler(MedicationSafetyException.class)
    ProblemDetail safety(MedicationSafetyException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY,
                "A critical safety issue blocks prescribing without an explicit clinical override.");
        detail.setTitle("Medication safety review required");
        detail.setType(URI.create("https://zantrix.org/problems/medication-safety-review"));
        detail.setProperty("assessment", exception.assessment());
        return detail;
    }
}
