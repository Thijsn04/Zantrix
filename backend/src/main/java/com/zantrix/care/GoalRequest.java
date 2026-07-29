package com.zantrix.care;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record GoalRequest(
        @NotBlank String patientId,
        @NotBlank String description,
        String priority,
        LocalDate targetDate,
        String addressesConditionId,
        String note) {
}
