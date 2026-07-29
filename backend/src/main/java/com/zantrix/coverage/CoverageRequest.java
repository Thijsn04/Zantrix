package com.zantrix.coverage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CoverageRequest(
        @NotBlank String patientId,
        @NotBlank String payorOrganizationId,
        @NotBlank String payorDisplay,
        @NotBlank String typeCode,
        @NotBlank String typeDisplay,
        @NotBlank String relationship,
        @NotBlank String subscriberId,
        String groupNumber,
        @NotNull LocalDate start,
        LocalDate end) {
}
