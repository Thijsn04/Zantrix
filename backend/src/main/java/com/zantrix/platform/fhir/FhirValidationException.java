package com.zantrix.platform.fhir;

import java.util.List;

/** Indicates that a resource did not satisfy the active FHIR validation chain. */
public final class FhirValidationException extends RuntimeException {
    private final List<String> issues;

    public FhirValidationException(List<String> issues) {
        super("FHIR resource validation failed: " + String.join("; ", issues));
        this.issues = List.copyOf(issues);
    }

    public List<String> issues() {
        return issues;
    }
}
