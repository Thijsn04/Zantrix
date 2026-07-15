package com.zantrix.problems;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record ProblemRequest(
        @NotBlank String patientId,
        String encounterId,
        @NotBlank String codeSystem,
        @NotBlank String code,
        @NotBlank String display,
        LocalDate onsetDate,
        String note) {
}
