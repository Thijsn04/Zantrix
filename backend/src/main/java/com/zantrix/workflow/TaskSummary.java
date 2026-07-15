package com.zantrix.workflow;

import java.time.Instant;

public record TaskSummary(String id, String patientId, String status, String priority,
                          String description, String focusReference, String ownerReference,
                          Instant authoredOn, Instant dueAt) { }
