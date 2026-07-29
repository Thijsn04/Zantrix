package com.zantrix.coverage;

import java.time.LocalDate;

public record CoverageSummary(
        String id,
        String patientId,
        String status,
        String typeCode,
        String type,
        String payor,
        String payorOrganizationId,
        String relationship,
        String subscriberId,
        String groupNumber,
        LocalDate start,
        LocalDate end) {
}
