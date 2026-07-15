package com.zantrix.patient;

import java.time.Instant;
import java.util.UUID;

public record PatientMergeSummary(UUID id, String sourcePatientId, String targetPatientId,
                                  String status, int resourcesRepointed, Instant mergedAt,
                                  Instant unmergedAt) { }
