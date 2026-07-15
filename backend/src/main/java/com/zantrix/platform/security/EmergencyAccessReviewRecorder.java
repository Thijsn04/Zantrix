package com.zantrix.platform.security;

import java.util.List;

public interface EmergencyAccessReviewRecorder {
    void record(String resourceType, String resourceId, String patientId);
    List<EmergencyAccessReview> openReviews();
    EmergencyAccessReview review(java.util.UUID id, String outcomeCode);
}
