package com.zantrix.platform.security;

import java.time.Instant;
import java.util.UUID;

public record EmergencyAccessReview(UUID id, Instant occurredAt, String actor, String reasonCode,
                                    String patientId, String resourceType, String resourceId,
                                    String taskId, String status, Instant reviewedAt,
                                    String reviewedBy, String outcomeCode) { }
