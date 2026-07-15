package com.zantrix.orders;

import jakarta.validation.constraints.NotBlank;

public record OrderRequest(
        @NotBlank String patientId,
        @NotBlank String encounterId,
        @NotBlank String requesterId,
        @NotBlank String category,
        @NotBlank String codeSystem,
        @NotBlank String code,
        @NotBlank String display,
        @NotBlank String priority,
        String clinicalNote) {
}
