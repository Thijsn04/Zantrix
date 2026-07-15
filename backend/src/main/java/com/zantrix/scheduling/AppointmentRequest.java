package com.zantrix.scheduling;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record AppointmentRequest(
        @NotBlank String patientId,
        @NotBlank String practitionerId,
        String locationId,
        String slotId,
        @NotNull Instant start,
        @NotNull Instant end,
        @NotBlank String serviceCode,
        @NotBlank String serviceDisplay,
        String comment) {
}
