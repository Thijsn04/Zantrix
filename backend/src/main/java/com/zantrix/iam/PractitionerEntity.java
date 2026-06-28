package com.zantrix.iam;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "practitioner")
public class PractitionerEntity {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false, unique = true)
    private String keycloakId;

    private String identifier; // BIG-nummer of UZI-nummer

    private boolean active = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String fhirData;

    protected PractitionerEntity() {}

    public PractitionerEntity(String keycloakId, String identifier, boolean active, String fhirData) {
        this.keycloakId = keycloakId;
        this.identifier = identifier;
        this.active = active;
        this.fhirData = fhirData;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getKeycloakId() { return keycloakId; }
    public void setKeycloakId(String keycloakId) { this.keycloakId = keycloakId; }
    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getFhirData() { return fhirData; }
    public void setFhirData(String fhirData) { this.fhirData = fhirData; }
}
