package com.zantrix.vitals;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record VitalMeasurement(
        @NotBlank String loincCode,
        @NotNull BigDecimal value,
        @NotBlank String unit,
        String note) {
}
