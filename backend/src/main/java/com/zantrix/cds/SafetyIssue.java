package com.zantrix.cds;

public record SafetyIssue(
        String ruleId,
        String severity,
        String summary,
        String existingMedicationId,
        String source) {
}
