package com.zantrix.immunizations;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ImmunizationRequest(
        @NotBlank String patientId,
        String encounterId,
        @NotBlank String performerId,
        @NotBlank String vaccineSystem,
        @NotBlank String vaccineCode,
        @NotBlank String vaccineDisplay,
        @NotNull LocalDate occurrenceDate,
        String lotNumber,
        String site,
        String route,
        Integer doseNumber,
        String note) {
}
