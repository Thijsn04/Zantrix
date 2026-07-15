package com.zantrix.medications;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PrescriptionRequest(
        @NotBlank String patientId,
        @NotBlank String encounterId,
        @NotBlank String requesterId,
        @NotBlank String rxNormIngredientCode,
        @NotBlank String medicationDisplay,
        @NotBlank String dosageText,
        String routeSystem,
        String routeCode,
        String routeDisplay,
        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal doseValue,
        @NotBlank String doseUnit,
        @Min(1) int frequencyPerDay,
        @Min(1) int durationDays,
        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal dispenseQuantity,
        @Min(0) int repeats,
        String reason,
        String safetyOverrideReason) {
}
