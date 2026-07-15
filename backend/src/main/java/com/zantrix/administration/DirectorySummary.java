package com.zantrix.administration;

public record DirectorySummary(String id, String resourceType, String name, boolean active,
                               String managingOrganizationId) { }
