package com.zantrix.immunizations;

import java.time.LocalDate;

public record ImmunizationSummary(
        String id,
        String patientId,
        String vaccineSystem,
        String vaccineCode,
        String vaccine,
        String status,
        LocalDate occurrenceDate,
        String lotNumber,
        String site,
        Integer doseNumber,
        String statusReason) {
}
