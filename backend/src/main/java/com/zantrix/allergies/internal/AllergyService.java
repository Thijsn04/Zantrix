package com.zantrix.allergies.internal;

import com.zantrix.allergies.AllergyRequest;
import com.zantrix.allergies.AllergySummary;
import com.zantrix.platform.fhir.FhirAccessGateway;
import com.zantrix.platform.fhir.FhirBundles;
import com.zantrix.terminology.TerminologyValidator;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class AllergyService {

    private final FhirAccessGateway fhir;
    private final TerminologyValidator terminology;

    public AllergyService(FhirAccessGateway fhir, TerminologyValidator terminology) {
        this.fhir = fhir;
        this.terminology = terminology;
    }

    public AllergySummary add(AllergyRequest request) {
        terminology.requireValid(request.substanceSystem(), request.substanceCode(), request.substanceDisplay());
        if (request.manifestationCode() != null && !request.manifestationCode().isBlank()) {
            terminology.requireValid(request.manifestationSystem(), request.manifestationCode(),
                    request.manifestationDisplay());
        }
        AllergyIntolerance allergy = new AllergyIntolerance();
        allergy.setId(UUID.randomUUID().toString());
        allergy.setPatient(new Reference("Patient/" + request.patientId()));
        if (request.encounterId() != null && !request.encounterId().isBlank()) {
            allergy.setEncounter(new Reference("Encounter/" + request.encounterId()));
        }
        allergy.setCode(new CodeableConcept(new Coding(request.substanceSystem(), request.substanceCode(),
                request.substanceDisplay())));
        allergy.setClinicalStatus(concept("http://terminology.hl7.org/CodeSystem/allergyintolerance-clinical", "active"));
        allergy.setVerificationStatus(concept(
                "http://terminology.hl7.org/CodeSystem/allergyintolerance-verification", "confirmed"));
        allergy.addCategory(AllergyIntolerance.AllergyIntoleranceCategory.fromCode(
                request.category().toLowerCase(Locale.ROOT)));
        allergy.setCriticality(AllergyIntolerance.AllergyIntoleranceCriticality.fromCode(
                request.criticality().toLowerCase(Locale.ROOT)));
        if (request.manifestationCode() != null && !request.manifestationCode().isBlank()) {
            AllergyIntolerance.AllergyIntoleranceReactionComponent reaction = allergy.addReaction();
            reaction.addManifestation(new CodeableConcept(new Coding(request.manifestationSystem(),
                    request.manifestationCode(), request.manifestationDisplay())));
            if (request.reactionSeverity() != null && !request.reactionSeverity().isBlank()) {
                reaction.setSeverity(AllergyIntolerance.AllergyIntoleranceSeverity.fromCode(
                        request.reactionSeverity().toLowerCase(Locale.ROOT)));
            }
        }
        if (request.note() != null && !request.note().isBlank()) {
            allergy.addNote().setText(request.note());
        }
        allergy.setRecordedDate(new Date());
        fhir.create(allergy, request.patientId());
        return summary(allergy);
    }

    public List<AllergySummary> list(String patientId, boolean includeInactive) {
        Map<String, List<String>> parameters = includeInactive
                ? Map.of("patient", List.of(patientId), "_sort", List.of("-date"), "_count", List.of("200"))
                : Map.of("patient", List.of(patientId), "clinical-status", List.of("active"),
                "_sort", List.of("-date"), "_count", List.of("200"));
        return FhirBundles.resources(fhir.search("AllergyIntolerance", parameters, patientId),
                AllergyIntolerance.class).stream().map(AllergyService::summary).toList();
    }

    public AllergySummary inactivate(String id, String patientId, boolean enteredInError) {
        AllergyIntolerance allergy = fhir.read(AllergyIntolerance.class, id, patientId);
        if (enteredInError) {
            allergy.setVerificationStatus(concept(
                    "http://terminology.hl7.org/CodeSystem/allergyintolerance-verification", "entered-in-error"));
        } else {
            allergy.setClinicalStatus(concept(
                    "http://terminology.hl7.org/CodeSystem/allergyintolerance-clinical", "inactive"));
        }
        fhir.update(allergy, patientId);
        return summary(allergy);
    }

    private static CodeableConcept concept(String system, String code) {
        return new CodeableConcept(new Coding(system, code, code));
    }

    private static AllergySummary summary(AllergyIntolerance allergy) {
        Coding substance = allergy.getCode().getCodingFirstRep();
        String reaction = allergy.getReaction().isEmpty() || allergy.getReactionFirstRep().getManifestation().isEmpty()
                ? null : allergy.getReactionFirstRep().getManifestationFirstRep().getCodingFirstRep().getDisplay();
        String severity = allergy.getReaction().isEmpty() || !allergy.getReactionFirstRep().hasSeverity()
                ? null : allergy.getReactionFirstRep().getSeverity().toCode();
        return new AllergySummary(allergy.getIdElement().getIdPart(),
                allergy.getPatient().getReferenceElement().getIdPart(), substance.getCode(), substance.getDisplay(),
                allergy.getClinicalStatus().getCodingFirstRep().getCode(),
                allergy.getVerificationStatus().getCodingFirstRep().getCode(),
                allergy.getCategory().isEmpty() ? null : allergy.getCategory().getFirst().getValue().toCode(),
                allergy.hasCriticality() ? allergy.getCriticality().toCode() : null, reaction, severity);
    }
}
