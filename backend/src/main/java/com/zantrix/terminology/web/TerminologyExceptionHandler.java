package com.zantrix.terminology.web;

import com.zantrix.terminology.TerminologyUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
class TerminologyExceptionHandler {

    @ExceptionHandler(TerminologyUnavailableException.class)
    ProblemDetail unavailable(TerminologyUnavailableException ignored) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "Coded clinical entry is unavailable until terminology validation can be restored.");
        detail.setTitle("Terminology service unavailable");
        detail.setType(URI.create("https://zantrix.org/problems/terminology-unavailable"));
        return detail;
    }
}
