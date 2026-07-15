package com.zantrix.administration;

import java.time.Instant;

public record FeatureFlagView(String name, boolean enabled, Instant updatedAt) { }
