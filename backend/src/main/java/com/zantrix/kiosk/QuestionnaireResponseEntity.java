package com.zantrix.kiosk;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.util.UUID;
import java.time.OffsetDateTime;

@Entity
@Table(name = "questionnaire_response")
public class QuestionnaireResponseEntity {
    
    @Id
    private UUID id = UUID.randomUUID();
    
    @Column(nullable = false)
    private UUID questionnaireId;
    
    @Column(nullable = false)
    private UUID patientId;
    
    private String status; // IN_PROGRESS, COMPLETED, AMENDED
    
    private OffsetDateTime authored = OffsetDateTime.now();
    
    @Column(columnDefinition = "jsonb")
    private String answers; // JSON structure of answers

    protected QuestionnaireResponseEntity() {}

    public QuestionnaireResponseEntity(UUID questionnaireId, UUID patientId, String status, String answers) {
        this.questionnaireId = questionnaireId;
        this.patientId = patientId;
        this.status = status;
        this.answers = answers;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getQuestionnaireId() { return questionnaireId; }
    public void setQuestionnaireId(UUID questionnaireId) { this.questionnaireId = questionnaireId; }
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public OffsetDateTime getAuthored() { return authored; }
    public void setAuthored(OffsetDateTime authored) { this.authored = authored; }
    public String getAnswers() { return answers; }
    public void setAnswers(String answers) { this.answers = answers; }
}
