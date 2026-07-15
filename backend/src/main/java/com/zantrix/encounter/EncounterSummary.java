package com.zantrix.encounter;

import java.time.Instant;

public record EncounterSummary(
        String id,
        String patientId,
        String practitionerId,
        String status,
        String reason,
        Instant start,
        Instant end) {
}
