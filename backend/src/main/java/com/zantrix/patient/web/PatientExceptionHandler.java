package com.zantrix.patient.web;

import com.zantrix.patient.MergeConflictException;
import com.zantrix.patient.PotentialDuplicateException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
class PatientExceptionHandler {

    @ExceptionHandler(PotentialDuplicateException.class)
    ProblemDetail duplicate(PotentialDuplicateException exception) {
        ProblemDetail detail = problem(HttpStatus.CONFLICT, "Potential duplicate patient",
                "Registration requires explicit duplicate review.", "potential-duplicate");
        detail.setProperty("candidates", exception.candidates());
        return detail;
    }

    @ExceptionHandler(MergeConflictException.class)
    ProblemDetail mergeConflict(MergeConflictException exception) {
        ProblemDetail detail = problem(HttpStatus.CONFLICT, "Patient unmerge conflict",
                "One or more resources changed after merge and require manual reconciliation.",
                "patient-unmerge-conflict");
        detail.setProperty("changedResources", exception.changedResources());
        return detail;
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail, String type) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create("https://zantrix.org/problems/" + type));
        return problem;
    }
}
