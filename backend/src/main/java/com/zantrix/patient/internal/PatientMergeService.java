package com.zantrix.patient.internal;

import ca.uhn.fhir.context.FhirContext;
import com.zantrix.patient.MergeConflictException;
import com.zantrix.patient.PatientMergeSummary;
import com.zantrix.platform.fhir.FhirAccessGateway;
import com.zantrix.platform.fhir.FhirBundles;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.DetectedIssue;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.Task;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PatientMergeService {

    private static final Map<String,String> PATIENT_SEARCH = searchParameters();
    private final FhirAccessGateway fhir;
    private final FhirContext fhirContext;
    private final PatientMergeJournal journal;
    private final PatientMergeRepository merges;
    public PatientMergeService(FhirAccessGateway fhir,FhirContext fhirContext,PatientMergeJournal journal,
                               PatientMergeRepository merges){
        this.fhir=fhir;this.fhirContext=fhirContext;this.journal=journal;this.merges=merges;
    }

    public PatientMergeSummary merge(String sourceId,String targetId){
        if(sourceId.equals(targetId))throw new IllegalArgumentException("Source and target patient must differ");
        Patient source=fhir.read(Patient.class,sourceId,sourceId);
        Patient target=fhir.read(Patient.class,targetId,targetId);
        if(!source.getActive()||!target.getActive())throw new IllegalArgumentException("Both patients must be active");
        List<Resource> resources=findReferences(sourceId);
        resources.forEach(resource->repoint(resource,sourceId,targetId));
        source.setActive(false);source.addLink().setOther(new Reference("Patient/"+targetId))
                .setType(Patient.LinkType.REPLACEDBY);
        target.addLink().setOther(new Reference("Patient/"+sourceId)).setType(Patient.LinkType.REPLACES);
        List<MergeManifest.MergedResource> items=resources.stream().map(resource->new MergeManifest.MergedResource(
                resource.fhirType(),resource.getIdElement().getIdPart(),fingerprint(resource))).toList();
        UUID mergeId=journal.prepare(sourceId,targetId,new MergeManifest(items));
        Bundle transaction=new Bundle().setType(Bundle.BundleType.TRANSACTION);
        resources.forEach(resource->addPut(transaction,resource));addPut(transaction,source);addPut(transaction,target);
        try{fhir.transaction(transaction,sourceId);journal.merged(mergeId);}
        catch(RuntimeException failure){journal.failed(mergeId,failure);throw failure;}
        return journal.entry(mergeId).summary(items.size());
    }

    public PatientMergeSummary unmerge(UUID mergeId){
        PatientMergeEntity merge=journal.entry(mergeId);
        if(!"MERGED".equals(merge.getStatus()))throw new IllegalArgumentException("Only a completed merge can be reversed");
        MergeManifest manifest=journal.manifest(merge);
        List<Resource> resources=new ArrayList<>();List<String> changed=new ArrayList<>();
        for(MergeManifest.MergedResource item:manifest.resources()){
            Resource resource=read(item.resourceType(),item.id(),merge.getTargetId());
            if(!fingerprint(resource).equals(item.postMergeFingerprint()))changed.add(item.resourceType()+"/"+item.id());
            resources.add(resource);
        }
        if(!changed.isEmpty())throw new MergeConflictException(changed);
        resources.forEach(resource->repoint(resource,merge.getTargetId(),merge.getSourceId()));
        Patient source=fhir.read(Patient.class,merge.getSourceId(),merge.getSourceId());
        Patient target=fhir.read(Patient.class,merge.getTargetId(),merge.getTargetId());
        source.setActive(true);source.getLink().removeIf(link->link.getType()==Patient.LinkType.REPLACEDBY
                && merge.getTargetId().equals(link.getOther().getReferenceElement().getIdPart()));
        target.getLink().removeIf(link->link.getType()==Patient.LinkType.REPLACES
                && merge.getSourceId().equals(link.getOther().getReferenceElement().getIdPart()));
        Bundle transaction=new Bundle().setType(Bundle.BundleType.TRANSACTION);
        resources.forEach(resource->addPut(transaction,resource));addPut(transaction,source);addPut(transaction,target);
        fhir.transaction(transaction,merge.getSourceId());journal.unmerged(mergeId);
        return journal.entry(mergeId).summary(resources.size());
    }

    public List<PatientMergeSummary> history(String patientId){
        return merges.findAllBySourceIdOrTargetIdOrderByPreparedAtDesc(patientId,patientId).stream()
                .map(value->value.summary(journal.manifest(value).resources().size())).toList();
    }

    private List<Resource> findReferences(String patientId){
        Map<String,Resource> unique=new LinkedHashMap<>();
        PATIENT_SEARCH.forEach((type,param)-> FhirBundles.resources(fhir.searchAll(type,
                Map.of(param,List.of(patientId),"_count",List.of("1000")),patientId),Resource.class)
                .forEach(resource->unique.put(resource.fhirType()+"/"+resource.getIdElement().getIdPart(),resource)));
        return new ArrayList<>(unique.values());
    }

    private Resource read(String type,String id,String patientId){return switch(type){
        case "Encounter"->fhir.read(Encounter.class,id,patientId);case "Appointment"->fhir.read(Appointment.class,id,patientId);
        case "Condition"->fhir.read(Condition.class,id,patientId);case "AllergyIntolerance"->fhir.read(AllergyIntolerance.class,id,patientId);
        case "MedicationRequest"->fhir.read(MedicationRequest.class,id,patientId);case "ServiceRequest"->fhir.read(ServiceRequest.class,id,patientId);
        case "Observation"->fhir.read(Observation.class,id,patientId);case "DiagnosticReport"->fhir.read(DiagnosticReport.class,id,patientId);
        case "Composition"->fhir.read(Composition.class,id,patientId);case "DocumentReference"->fhir.read(DocumentReference.class,id,patientId);
        case "Consent"->fhir.read(Consent.class,id,patientId);case "Task"->fhir.read(Task.class,id,patientId);
        case "DetectedIssue"->fhir.read(DetectedIssue.class,id,patientId);default->throw new IllegalStateException("Unsupported merge resource "+type);};}

    private static void repoint(Resource resource,String source,String target){
        Reference replacement=new Reference("Patient/"+target);
        if(resource instanceof Encounter value)value.setSubject(replacement);
        else if(resource instanceof Appointment value)value.getParticipant().stream()
                .filter(p->source.equals(p.getActor().getReferenceElement().getIdPart())&&"Patient".equals(p.getActor().getReferenceElement().getResourceType()))
                .forEach(p->p.setActor(replacement));
        else if(resource instanceof Condition value)value.setSubject(replacement);
        else if(resource instanceof AllergyIntolerance value)value.setPatient(replacement);
        else if(resource instanceof MedicationRequest value)value.setSubject(replacement);
        else if(resource instanceof ServiceRequest value)value.setSubject(replacement);
        else if(resource instanceof Observation value)value.setSubject(replacement);
        else if(resource instanceof DiagnosticReport value)value.setSubject(replacement);
        else if(resource instanceof Composition value)value.setSubject(replacement);
        else if(resource instanceof DocumentReference value)value.setSubject(replacement);
        else if(resource instanceof Consent value)value.setPatient(replacement);
        else if(resource instanceof Task value)value.setFor(replacement);
        else if(resource instanceof DetectedIssue value)value.setPatient(replacement);
        else throw new IllegalStateException("Unsupported merge resource "+resource.fhirType());
    }

    private String fingerprint(Resource resource){
        Resource copy=(Resource)resource.copy();copy.setMeta(null);
        String json=fhirContext.newJsonParser().setPrettyPrint(false).encodeResourceToString(copy);
        try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(json.getBytes(StandardCharsets.UTF_8)));}
        catch(NoSuchAlgorithmException impossible){throw new IllegalStateException("SHA-256 is unavailable",impossible);}
    }
    private static void addPut(Bundle bundle,Resource resource){bundle.addEntry().setResource(resource).getRequest()
            .setMethod(Bundle.HTTPVerb.PUT).setUrl(resource.fhirType()+"/"+resource.getIdElement().getIdPart());}
    private static Map<String,String> searchParameters(){Map<String,String> values=new LinkedHashMap<>();
        values.put("Encounter","patient");values.put("Appointment","patient");values.put("Condition","patient");
        values.put("AllergyIntolerance","patient");values.put("MedicationRequest","patient");values.put("ServiceRequest","patient");
        values.put("Observation","patient");values.put("DiagnosticReport","patient");values.put("Composition","patient");
        values.put("DocumentReference","patient");values.put("Consent","patient");values.put("Task","patient");
        values.put("DetectedIssue","patient");return Map.copyOf(values);}
}
