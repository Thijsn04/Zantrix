package com.zantrix.patient;

import java.time.LocalDate;

public record PatientSummary(
        String id,
        String displayName,
        LocalDate birthDate,
        String administrativeGender,
        boolean active,
        String identifier,
        double duplicateScore) {
}
