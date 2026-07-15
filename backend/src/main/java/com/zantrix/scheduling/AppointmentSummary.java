package com.zantrix.scheduling;

import java.time.Instant;

public record AppointmentSummary(
        String id,
        String patientId,
        String practitionerId,
        String locationId,
        String status,
        String service,
        Instant start,
        Instant end) {
}
