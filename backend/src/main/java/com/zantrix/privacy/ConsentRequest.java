package com.zantrix.privacy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public record ConsentRequest(@NotBlank String patientId, @NotBlank String type,
                             @NotEmpty List<@NotBlank String> resourceTypes,
                             @NotNull Instant start, Instant end, String policyUri) { }
