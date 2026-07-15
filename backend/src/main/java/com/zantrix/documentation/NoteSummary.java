package com.zantrix.documentation;

import java.time.Instant;

public record NoteSummary(String id, String patientId, String encounterId, String title,
                          String type, String status, String authorId, Instant date, int version) { }
