package com.zantrix.medications;

import com.zantrix.cds.SafetyAssessment;

public final class MedicationSafetyException extends RuntimeException {
    private final SafetyAssessment assessment;
    public MedicationSafetyException(SafetyAssessment assessment) {
        super("Medication safety review requires an explicit clinical decision");
        this.assessment = assessment;
    }
    public SafetyAssessment assessment() { return assessment; }
}
