package com.zantrix.problems;

import java.time.LocalDate;

public record ProblemSummary(
        String id,
        String patientId,
        String codeSystem,
        String code,
        String display,
        String clinicalStatus,
        LocalDate onsetDate,
        LocalDate resolvedDate) {
}
