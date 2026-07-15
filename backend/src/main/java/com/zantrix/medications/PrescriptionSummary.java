package com.zantrix.medications;

import com.zantrix.cds.SafetyAssessment;

public record PrescriptionSummary(
        String id,
        String patientId,
        String rxNormIngredientCode,
        String medication,
        String status,
        String dosage,
        SafetyAssessment safety) {
}
