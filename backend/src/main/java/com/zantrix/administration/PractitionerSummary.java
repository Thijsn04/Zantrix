package com.zantrix.administration;

public record PractitionerSummary(String id, String identitySubject, String displayName,
                                  String roleCode, String roleDisplay, String organizationId,
                                  String locationId, boolean active) {
}
