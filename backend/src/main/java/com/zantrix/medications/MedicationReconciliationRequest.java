package com.zantrix.medications;

import jakarta.validation.constraints.NotBlank;

public record MedicationReconciliationRequest(@NotBlank String patientId, String encounterId,
                                              @NotBlank String performerId,
                                              @NotBlank String rxNormIngredientCode,
                                              @NotBlank String medicationDisplay,
                                              @NotBlank String status, String dosageText,
                                              String informationSource) {
}
