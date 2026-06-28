package com.zantrix.workflow;

import jakarta.persistence.*;
import java.util.UUID;


@Entity
@Table(name = "workflow_definition")
public class WorkflowDefinitionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean active;

    @Column(columnDefinition = "jsonb")
    private String stepsConfig; // e.g., JSON representing the flow of tasks

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getStepsConfig() { return stepsConfig; }
    public void setStepsConfig(String stepsConfig) { this.stepsConfig = stepsConfig; }
}
