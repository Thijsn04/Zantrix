package com.zantrix.care;

import java.time.LocalDate;

public record GoalSummary(String id, String patientId, String description, String lifecycleStatus,
                          String achievementStatus, String priority, LocalDate targetDate,
                          String addressesConditionId) {
}
