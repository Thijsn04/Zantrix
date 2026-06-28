package com.zantrix.charting.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.util.UUID;

@Entity
@Table(name = "smart_template")
public class SmartTemplateEntity {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false, unique = true)
    private String shortcut; // e.g. ".bp" or ".ecg_normaal"

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String contentTemplate; // The actual rich text or JSONB template

    private UUID authorId; // Null if it's a global/department template, set if it's a personal macro

    protected SmartTemplateEntity() {}

    public SmartTemplateEntity(String shortcut, String title, String contentTemplate, UUID authorId) {
        this.shortcut = shortcut;
        this.title = title;
        this.contentTemplate = contentTemplate;
        this.authorId = authorId;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getShortcut() { return shortcut; }
    public void setShortcut(String shortcut) { this.shortcut = shortcut; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContentTemplate() { return contentTemplate; }
    public void setContentTemplate(String contentTemplate) { this.contentTemplate = contentTemplate; }
    public UUID getAuthorId() { return authorId; }
    public void setAuthorId(UUID authorId) { this.authorId = authorId; }
}
