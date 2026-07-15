package com.zantrix.encounter.internal;

import com.zantrix.encounter.EncounterRequest;
import com.zantrix.encounter.EncounterSummary;
import com.zantrix.platform.fhir.FhirAccessGateway;
import com.zantrix.platform.fhir.FhirBundles;
import com.zantrix.terminology.TerminologyValidator;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class EncounterService {

    private static final String SNOMED = "http://snomed.info/sct";
    private final FhirAccessGateway fhir;
    private final TerminologyValidator terminology;

    public EncounterService(FhirAccessGateway fhir, TerminologyValidator terminology) {
        this.fhir = fhir;
        this.terminology = terminology;
    }

    public EncounterSummary create(EncounterRequest request) {
        terminology.requireValid(SNOMED, request.reasonCode(), request.reasonDisplay());
        Encounter encounter = new Encounter();
        encounter.setId(UUID.randomUUID().toString());
        encounter.setStatus(Encounter.EncounterStatus.PLANNED);
        encounter.setClass_(new Coding("http://terminology.hl7.org/CodeSystem/v3-ActCode", "AMB", "ambulatory"));
        encounter.setSubject(new Reference("Patient/" + request.patientId()));
        encounter.addParticipant().setIndividual(new Reference("Practitioner/" + request.practitionerId()));
        encounter.addReasonCode(new CodeableConcept(new Coding(SNOMED, request.reasonCode(), request.reasonDisplay())));
        if (request.organizationId() != null && !request.organizationId().isBlank()) {
            encounter.setServiceProvider(new Reference("Organization/" + request.organizationId()));
        }
        if (request.appointmentId() != null && !request.appointmentId().isBlank()) {
            encounter.addAppointment(new Reference("Appointment/" + request.appointmentId()));
        }
        if (request.plannedStart() != null) {
            encounter.getPeriod().setStart(Date.from(request.plannedStart()));
        }
        fhir.create(encounter, request.patientId());
        return summary(encounter);
    }

    public EncounterSummary get(String id, String patientId) {
        return summary(fhir.read(Encounter.class, id, patientId));
    }

    public List<EncounterSummary> list(String patientId) {
        return FhirBundles.resources(fhir.search("Encounter", Map.of(
                        "patient", List.of(patientId), "_sort", List.of("-date"), "_count", List.of("100")),
                patientId), Encounter.class).stream().map(EncounterService::summary).toList();
    }

    public EncounterSummary start(String id, String patientId) {
        Encounter encounter = fhir.read(Encounter.class, id, patientId);
        requireStatus(encounter, Encounter.EncounterStatus.PLANNED, Encounter.EncounterStatus.ARRIVED);
        encounter.setStatus(Encounter.EncounterStatus.INPROGRESS);
        if (!encounter.getPeriod().hasStart() || encounter.getPeriod().getStart().after(new Date())) {
            encounter.getPeriod().setStart(new Date());
        }
        fhir.update(encounter, patientId);
        return summary(encounter);
    }

    public EncounterSummary finish(String id, String patientId) {
        Encounter encounter = fhir.read(Encounter.class, id, patientId);
        requireStatus(encounter, Encounter.EncounterStatus.INPROGRESS);
        encounter.setStatus(Encounter.EncounterStatus.FINISHED);
        encounter.getPeriod().setEnd(new Date());
        fhir.update(encounter, patientId);
        return summary(encounter);
    }

    public EncounterSummary cancel(String id, String patientId) {
        Encounter encounter = fhir.read(Encounter.class, id, patientId);
        requireStatus(encounter, Encounter.EncounterStatus.PLANNED, Encounter.EncounterStatus.ARRIVED);
        encounter.setStatus(Encounter.EncounterStatus.CANCELLED);
        fhir.update(encounter, patientId);
        return summary(encounter);
    }

    private static void requireStatus(Encounter encounter, Encounter.EncounterStatus... allowed) {
        for (Encounter.EncounterStatus status : allowed) {
            if (encounter.getStatus() == status) {
                return;
            }
        }
        throw new IllegalArgumentException("Encounter transition is not valid from status "
                + encounter.getStatus().toCode());
    }

    private static EncounterSummary summary(Encounter encounter) {
        Instant start = encounter.getPeriod().getStart() == null ? null
                : encounter.getPeriod().getStart().toInstant();
        Instant end = encounter.getPeriod().getEnd() == null ? null
                : encounter.getPeriod().getEnd().toInstant();
        String practitioner = encounter.getParticipant().isEmpty() ? null
                : encounter.getParticipantFirstRep().getIndividual().getReferenceElement().getIdPart();
        String reason = encounter.getReasonCode().isEmpty() ? null
                : encounter.getReasonCodeFirstRep().getText();
        if ((reason == null || reason.isBlank()) && !encounter.getReasonCode().isEmpty()
                && !encounter.getReasonCodeFirstRep().getCoding().isEmpty()) {
            reason = encounter.getReasonCodeFirstRep().getCodingFirstRep().getDisplay();
        }
        return new EncounterSummary(encounter.getIdElement().getIdPart(),
                encounter.getSubject().getReferenceElement().getIdPart(), practitioner,
                encounter.getStatus().toCode(), reason, start, end);
    }
}
