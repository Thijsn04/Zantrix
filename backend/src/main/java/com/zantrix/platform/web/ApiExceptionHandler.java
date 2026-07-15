package com.zantrix.platform.web;

import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import com.zantrix.platform.fhir.FhirAuditPendingException;
import com.zantrix.platform.fhir.FhirValidationException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

/** RFC 9457 error responses without resource bodies or clinical detail. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(FhirAuditPendingException.class)
    ProblemDetail auditPending(FhirAuditPendingException exception) {
        ProblemDetail detail = problem(HttpStatus.SERVICE_UNAVAILABLE, "Audit reconciliation pending",
                "The clinical mutation committed and must not be blindly retried.", "audit-pending");
        detail.setProperty("operationId", exception.operationId());
        return detail;
    }

    @ExceptionHandler(FhirValidationException.class)
    ProblemDetail validation(FhirValidationException exception) {
        ProblemDetail detail = problem(HttpStatus.UNPROCESSABLE_ENTITY, "FHIR validation failed",
                "The resource does not satisfy the active FHIR profiles.", "fhir-validation-failed");
        detail.setProperty("issues", exception.issues());
        return detail;
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    ProblemDetail invalidRequest(Exception ignored) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request",
                "One or more request fields are invalid.", "invalid-request");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail invalidArgument(IllegalArgumentException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", exception.getMessage(), "invalid-request");
    }

    @ExceptionHandler(BaseServerResponseException.class)
    ProblemDetail fhirFailure(BaseServerResponseException exception) {
        HttpStatus status = HttpStatus.resolve(exception.getStatusCode());
        if (status == null) {
            status = HttpStatus.BAD_GATEWAY;
        }
        return problem(status, "FHIR operation failed",
                "The clinical data service rejected the operation.", "fhir-operation-failed");
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail, String type) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create("https://zantrix.org/problems/" + type));
        return problem;
    }
}
