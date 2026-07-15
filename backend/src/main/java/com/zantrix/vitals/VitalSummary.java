package com.zantrix.vitals;

import java.math.BigDecimal;
import java.time.Instant;

public record VitalSummary(String id, String loincCode, String display, BigDecimal value,
                           String unit, Instant observedAt) {
}
