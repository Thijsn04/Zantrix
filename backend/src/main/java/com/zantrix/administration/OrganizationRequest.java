package com.zantrix.administration;

import jakarta.validation.constraints.NotBlank;

public record OrganizationRequest(@NotBlank String name, String identifierSystem,
                                  String identifierValue) { }
