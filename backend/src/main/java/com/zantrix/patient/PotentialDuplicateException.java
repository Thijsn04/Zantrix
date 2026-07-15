package com.zantrix.patient;

import java.util.List;

public final class PotentialDuplicateException extends RuntimeException {

    private final List<PatientSummary> candidates;

    public PotentialDuplicateException(List<PatientSummary> candidates) {
        super("Patient registration requires duplicate review");
        this.candidates = List.copyOf(candidates);
    }

    public List<PatientSummary> candidates() {
        return candidates;
    }
}
