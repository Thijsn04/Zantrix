package com.zantrix.allergies;

import jakarta.validation.constraints.NotBlank;

public record AllergyRequest(
        @NotBlank String patientId,
        String encounterId,
        @NotBlank String substanceSystem,
        @NotBlank String substanceCode,
        @NotBlank String substanceDisplay,
        @NotBlank String category,
        @NotBlank String criticality,
        String manifestationSystem,
        String manifestationCode,
        String manifestationDisplay,
        String reactionSeverity,
        String note) {
}
