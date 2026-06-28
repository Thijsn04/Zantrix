package com.zantrix.kiosk;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.util.UUID;

@Entity
@Table(name = "questionnaire")
public class QuestionnaireEntity {
    
    @Id
    private UUID id = UUID.randomUUID();
    
    @Column(nullable = false, unique = true)
    private String name;
    
    private String status; // DRAFT, ACTIVE, RETIRED
    
    @Column(columnDefinition = "jsonb")
    private String items; // The JSON structure of questions

    protected QuestionnaireEntity() {}

    public QuestionnaireEntity(String name, String status, String items) {
        this.name = name;
        this.status = status;
        this.items = items;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getItems() { return items; }
    public void setItems(String items) { this.items = items; }
}
