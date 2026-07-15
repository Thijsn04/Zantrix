package com.zantrix.encounter;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record EncounterRequest(
        @NotBlank String patientId,
        @NotBlank String practitionerId,
        String organizationId,
        String appointmentId,
        @NotBlank String reasonCode,
        @NotBlank String reasonDisplay,
        Instant plannedStart) {
}
