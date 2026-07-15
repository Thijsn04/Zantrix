package com.zantrix.vitals;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public record VitalsRequest(
        @NotBlank String patientId,
        @NotBlank String encounterId,
        @NotBlank String performerId,
        @NotNull Instant observedAt,
        @NotEmpty List<@Valid VitalMeasurement> measurements) {
}
