package com.zantrix.privacy;

import java.time.Instant;
import java.util.List;

public record ConsentSummary(String id, String patientId, String status, String type,
                             List<String> resourceTypes, Instant start, Instant end) {
    public ConsentSummary { resourceTypes = List.copyOf(resourceTypes); }
}
