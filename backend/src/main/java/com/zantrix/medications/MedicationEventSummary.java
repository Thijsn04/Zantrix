package com.zantrix.medications;

import java.time.Instant;

public record MedicationEventSummary(String id, String resourceType, String patientId,
                                     String prescriptionId, String status, Instant occurredAt) {
}
