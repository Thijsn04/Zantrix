package com.zantrix.orders;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public record ResultRequest(
        @NotBlank String patientId,
        @NotBlank String performerId,
        @NotNull Instant issuedAt,
        @NotEmpty List<@Valid ResultMeasurement> measurements,
        String conclusion) {
}
