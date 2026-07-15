package com.zantrix.patient.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
class PatientMergeJournal {
    private final PatientMergeRepository merges;
    private final ObjectMapper json;
    PatientMergeJournal(PatientMergeRepository merges,ObjectMapper json){this.merges=merges;this.json=json;}
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public UUID prepare(String source,String target,MergeManifest manifest){
        UUID id=UUID.randomUUID();
        merges.save(new PatientMergeEntity(id,source,target,actor(),write(manifest)));return id;
    }
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void merged(UUID id){entry(id).merged();}
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void failed(UUID id,Throwable failure){entry(id).failed(failure);}
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void unmerged(UUID id){entry(id).unmerged(actor());}
    @Transactional(readOnly=true) public PatientMergeEntity entry(UUID id){return merges.findById(id)
            .orElseThrow(()->new IllegalArgumentException("Patient merge not found"));}
    MergeManifest manifest(PatientMergeEntity value){try{return json.readValue(value.getManifest(),MergeManifest.class);}
        catch(JsonProcessingException failure){throw new IllegalStateException("Patient merge manifest is unreadable",failure);}}
    private String write(MergeManifest value){try{return json.writeValueAsString(value);}
        catch(JsonProcessingException failure){throw new IllegalStateException("Patient merge manifest cannot be written",failure);}}
    private static String actor(){return SecurityContextHolder.getContext().getAuthentication().getName();}
}
