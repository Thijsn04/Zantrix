package com.zantrix.platform.fhir.internal;

import com.zantrix.platform.security.EmergencyAccessReview;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="emergency_access_review")
class EmergencyAccessReviewEntity {
    @Id private UUID id;
    private Instant occurredAt;
    private String actor;
    private String reasonCode;
    private String patientId;
    private String resourceType;
    private String resourceId;
    private String taskId;
    private String status;
    private Instant reviewedAt;
    private String reviewedBy;
    private String outcomeCode;
    protected EmergencyAccessReviewEntity() { }
    EmergencyAccessReviewEntity(UUID id,Instant occurredAt,String actor,String reasonCode,String patientId,
                                String resourceType,String resourceId){
        this.id=id;this.occurredAt=occurredAt;this.actor=actor;this.reasonCode=reasonCode;
        this.patientId=patientId;this.resourceType=resourceType;this.resourceId=resourceId;this.status="OPEN";
    }
    UUID getId(){return id;} String getPatientId(){return patientId;} String getTaskId(){return taskId;}
    void taskCreated(String value){taskId=value;}
    void reviewed(String reviewer,String outcome){status="REVIEWED";reviewedAt=Instant.now();reviewedBy=reviewer;outcomeCode=outcome;}
    EmergencyAccessReview view(){return new EmergencyAccessReview(id,occurredAt,actor,reasonCode,patientId,resourceType,
            resourceId,taskId,status,reviewedAt,reviewedBy,outcomeCode);}
}
