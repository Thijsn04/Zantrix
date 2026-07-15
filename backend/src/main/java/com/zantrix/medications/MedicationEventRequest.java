package com.zantrix.medications;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record MedicationEventRequest(@NotBlank String patientId, @NotBlank String prescriptionId,
                                     String encounterId, @NotBlank String performerId,
                                     @NotNull Instant occurredAt,
                                     @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal quantity,
                                     @NotBlank String unit, String note) {
}
