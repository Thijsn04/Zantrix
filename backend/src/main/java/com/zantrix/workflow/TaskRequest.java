package com.zantrix.workflow;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record TaskRequest(@NotBlank String patientId, String encounterId, String focusReference,
                          @NotBlank String description, String ownerReference, String priority,
                          Instant dueAt) { }
