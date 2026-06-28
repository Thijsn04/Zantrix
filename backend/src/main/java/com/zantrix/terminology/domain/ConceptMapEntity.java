package com.zantrix.terminology.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;


@Entity
@Table(name = "concept_map")
public class ConceptMapEntity {
    
    @Id
    private UUID id = UUID.randomUUID();
    
    private String sourceSystem;
    
    private String sourceCode;
    
    private String targetSystem;
    
    private String targetCode;
    
    private String equivalence; // equivalent, relatedto, narrower, broader

    protected ConceptMapEntity() {}

    public ConceptMapEntity(String sourceSystem, String sourceCode, String targetSystem, String targetCode, String equivalence) {
        this.sourceSystem = sourceSystem;
        this.sourceCode = sourceCode;
        this.targetSystem = targetSystem;
        this.targetCode = targetCode;
        this.equivalence = equivalence;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getSourceSystem() { return sourceSystem; }
    public void setSourceSystem(String sourceSystem) { this.sourceSystem = sourceSystem; }
    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }
    public String getTargetSystem() { return targetSystem; }
    public void setTargetSystem(String targetSystem) { this.targetSystem = targetSystem; }
    public String getTargetCode() { return targetCode; }
    public void setTargetCode(String targetCode) { this.targetCode = targetCode; }
    public String getEquivalence() { return equivalence; }
    public void setEquivalence(String equivalence) { this.equivalence = equivalence; }
}
