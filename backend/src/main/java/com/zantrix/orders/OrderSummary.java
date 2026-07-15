package com.zantrix.orders;

import java.time.Instant;

public record OrderSummary(String id, String patientId, String encounterId, String category,
                           String code, String display, String status, String priority,
                           Instant authoredOn, String taskId) {
}
