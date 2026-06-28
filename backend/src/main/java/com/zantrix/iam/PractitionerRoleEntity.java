package com.zantrix.iam;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "practitioner_role")
public class PractitionerRoleEntity {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private UUID practitionerId;

    @Column(nullable = false)
    private UUID organizationId;

    private String roleCode;

    private String specialty;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String fhirData;

    protected PractitionerRoleEntity() {}

    public PractitionerRoleEntity(UUID practitionerId, UUID organizationId, String roleCode, String specialty, String fhirData) {
        this.practitionerId = practitionerId;
        this.organizationId = organizationId;
        this.roleCode = roleCode;
        this.specialty = specialty;
        this.fhirData = fhirData;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPractitionerId() { return practitionerId; }
    public void setPractitionerId(UUID practitionerId) { this.practitionerId = practitionerId; }
    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }
    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public String getFhirData() { return fhirData; }
    public void setFhirData(String fhirData) { this.fhirData = fhirData; }
}
