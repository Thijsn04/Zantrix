package com.zantrix.cds;

public interface MedicationSafetyChecker {

    SafetyAssessment assess(String patientId, String rxNormIngredientCode, String display);
}
