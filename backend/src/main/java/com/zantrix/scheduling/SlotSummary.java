package com.zantrix.scheduling;

import java.time.Instant;

public record SlotSummary(String id, String scheduleId, String practitionerId, String locationId,
                          String serviceDisplay, String status, Instant start, Instant end) {
}
