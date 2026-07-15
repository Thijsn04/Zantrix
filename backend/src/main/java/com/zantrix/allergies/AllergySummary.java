package com.zantrix.allergies;

public record AllergySummary(
        String id,
        String patientId,
        String substanceCode,
        String substance,
        String clinicalStatus,
        String verificationStatus,
        String category,
        String criticality,
        String reaction,
        String severity) {
}
