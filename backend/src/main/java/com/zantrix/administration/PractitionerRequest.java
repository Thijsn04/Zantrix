package com.zantrix.administration;

import jakarta.validation.constraints.NotBlank;

public record PractitionerRequest(@NotBlank String identitySubject, @NotBlank String familyName,
                                  String givenName, @NotBlank String roleCode, String roleDisplay,
                                  @NotBlank String organizationId, String locationId) {
}
