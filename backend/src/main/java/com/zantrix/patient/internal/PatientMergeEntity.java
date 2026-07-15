package com.zantrix.patient.internal;

import com.zantrix.patient.PatientMergeSummary;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="patient_merge")
class PatientMergeEntity {
    @Id private UUID id;
    private String sourceId;
    private String targetId;
    private String status;
    private Instant preparedAt;
    private Instant mergedAt;
    private String mergedBy;
    private Instant unmergedAt;
    private String unmergedBy;
    private String manifest;
    private String errorCode;
    @Version private long lockVersion;
    protected PatientMergeEntity(){ }
    PatientMergeEntity(UUID id,String sourceId,String targetId,String actor,String manifest){
        this.id=id;this.sourceId=sourceId;this.targetId=targetId;this.status="PREPARED";
        this.preparedAt=Instant.now();this.mergedBy=actor;this.manifest=manifest;
    }
    UUID getId(){return id;} String getSourceId(){return sourceId;} String getTargetId(){return targetId;}
    String getStatus(){return status;} String getManifest(){return manifest;}
    void merged(){status="MERGED";mergedAt=Instant.now();errorCode=null;}
    void failed(Throwable failure){status="FAILED";errorCode=failure.getClass().getSimpleName();}
    void unmerged(String actor){status="UNMERGED";unmergedAt=Instant.now();unmergedBy=actor;}
    PatientMergeSummary summary(int count){return new PatientMergeSummary(id,sourceId,targetId,status,count,mergedAt,unmergedAt);}
}
