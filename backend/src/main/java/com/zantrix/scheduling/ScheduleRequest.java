package com.zantrix.scheduling;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public record ScheduleRequest(@NotBlank String practitionerId, String locationId,
                              @NotBlank String serviceCode, @NotBlank String serviceDisplay,
                              @NotEmpty List<@Valid SlotInterval> slots) {
    public record SlotInterval(@NotNull Instant start, @NotNull Instant end) { }
}
