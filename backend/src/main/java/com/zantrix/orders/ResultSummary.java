package com.zantrix.orders;

import java.time.Instant;
import java.util.List;

public record ResultSummary(String id, String orderId, String patientId, String status,
                            String conclusion, Instant issuedAt, List<String> observationIds) {
    public ResultSummary { observationIds = List.copyOf(observationIds); }
}
