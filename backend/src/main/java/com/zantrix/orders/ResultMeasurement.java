package com.zantrix.orders;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record ResultMeasurement(
        @NotBlank String codeSystem,
        @NotBlank String code,
        @NotBlank String display,
        BigDecimal numericValue,
        String unit,
        String textValue,
        BigDecimal referenceLow,
        BigDecimal referenceHigh,
        String interpretationCode) {

    public ResultMeasurement {
        if ((numericValue == null) == (textValue == null || textValue.isBlank())) {
            throw new IllegalArgumentException("Exactly one numericValue or textValue is required");
        }
        if (numericValue != null && (unit == null || unit.isBlank())) {
            throw new IllegalArgumentException("A UCUM unit is required for a numeric result");
        }
    }
}
