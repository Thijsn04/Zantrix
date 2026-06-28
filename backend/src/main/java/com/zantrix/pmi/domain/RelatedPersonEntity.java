package com.zantrix.pmi.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.util.UUID;

@Entity
@Table(name = "related_person")
public class RelatedPersonEntity {
    
    @Id
    private UUID id = UUID.randomUUID();
    
    @Column(nullable = false)
    private UUID patientId;
    
    private String name;
    
    private String relationship; // e.g. Mother, Emergency Contact
    
    private String phone;
    
    protected RelatedPersonEntity() {}

    public RelatedPersonEntity(UUID patientId, String name, String relationship, String phone) {
        this.patientId = patientId;
        this.name = name;
        this.relationship = relationship;
        this.phone = phone;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
