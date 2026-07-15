package com.zantrix.administration.internal;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity @Table(name = "feature_flag")
class FeatureFlagEntity {
    @Id private String name;
    private boolean enabled;
    private Instant updatedAt;
    private String updatedBy;
    protected FeatureFlagEntity() { }
    FeatureFlagEntity(String name, boolean enabled, Instant updatedAt, String updatedBy) {
        this.name=name; this.enabled=enabled; this.updatedAt=updatedAt; this.updatedBy=updatedBy;
    }
    String getName(){return name;} boolean isEnabled(){return enabled;} Instant getUpdatedAt(){return updatedAt;}
    void update(boolean value, String actor){enabled=value; updatedAt=Instant.now(); updatedBy=actor;}
}
