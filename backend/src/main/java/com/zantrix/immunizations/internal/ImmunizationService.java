package com.zantrix.immunizations.internal;

import com.zantrix.immunizations.ImmunizationRequest;
import com.zantrix.immunizations.ImmunizationSummary;
import com.zantrix.platform.fhir.FhirAccessGateway;
import com.zantrix.platform.fhir.FhirBundles;
import com.zantrix.terminology.TerminologyValidator;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.PositiveIntType;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Vaccination history.
 *
 * A recorded vaccination is a statement that a dose was given, so it is never
 * deleted. A mistaken entry is marked entered-in-error and stays visible in the
 * history, because a record that quietly loses a dose is more dangerous than
 * one that shows a correction.
 */
@Service
public class ImmunizationService {

    private static final String STATUS_REASON = "http://terminology.hl7.org/CodeSystem/v3-ActReason";
    private final FhirAccessGateway fhir;
    private final TerminologyValidator terminology;

    public ImmunizationService(FhirAccessGateway fhir, TerminologyValidator terminology) {
        this.fhir = fhir;
        this.terminology = terminology;
    }

    public ImmunizationSummary record(ImmunizationRequest request) {
        terminology.requireValid(request.vaccineSystem(), request.vaccineCode(), request.vaccineDisplay());
        if (request.occurrenceDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A vaccination cannot be recorded in the future");
        }
        if (request.doseNumber() != null && request.doseNumber() < 1) {
            throw new IllegalArgumentException("Dose number must be 1 or higher");
        }

        Immunization immunization = new Immunization();
        immunization.setId(UUID.randomUUID().toString());
        immunization.setStatus(Immunization.ImmunizationStatus.COMPLETED);
        immunization.setPatient(new Reference("Patient/" + request.patientId()));
        if (request.encounterId() != null && !request.encounterId().isBlank()) {
            immunization.setEncounter(new Reference("Encounter/" + request.encounterId()));
        }
        immunization.setVaccineCode(new CodeableConcept(new Coding(
                request.vaccineSystem(), request.vaccineCode(), request.vaccineDisplay())));
        immunization.setOccurrence(new DateTimeType(request.occurrenceDate().toString()));
        immunization.setPrimarySource(true);
        immunization.addPerformer().setActor(new Reference("Practitioner/" + request.performerId()));
        if (request.lotNumber() != null && !request.lotNumber().isBlank()) {
            immunization.setLotNumber(request.lotNumber());
        }
        if (request.site() != null && !request.site().isBlank()) {
            immunization.setSite(new CodeableConcept().setText(request.site()));
        }
        if (request.route() != null && !request.route().isBlank()) {
            immunization.setRoute(new CodeableConcept().setText(request.route()));
        }
        if (request.doseNumber() != null) {
            immunization.addProtocolApplied().setDoseNumber(new PositiveIntType(request.doseNumber()));
        }
        if (request.note() != null && !request.note().isBlank()) {
            immunization.addNote().setText(request.note());
        }

        fhir.create(immunization, request.patientId());
        return summary(immunization);
    }

    public List<ImmunizationSummary> list(String patientId, boolean includeEnteredInError) {
        List<Immunization> found = FhirBundles.resources(fhir.search("Immunization", Map.of(
                "patient", List.of(patientId), "_sort", List.of("-date"), "_count", List.of("200")),
                patientId), Immunization.class);
        return found.stream()
                .filter(value -> includeEnteredInError
                        || value.getStatus() != Immunization.ImmunizationStatus.ENTEREDINERROR)
                .map(ImmunizationService::summary)
                .toList();
    }

    /** Corrects a mistaken entry without removing it, so the history stays complete. */
    public ImmunizationSummary markEnteredInError(String id, String patientId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("A reason is required to mark a vaccination entered in error");
        }
        Immunization immunization = fhir.read(Immunization.class, id, patientId);
        if (immunization.getStatus() == Immunization.ImmunizationStatus.ENTEREDINERROR) {
            throw new IllegalArgumentException("This vaccination is already marked entered in error");
        }
        immunization.setStatus(Immunization.ImmunizationStatus.ENTEREDINERROR);
        immunization.setStatusReason(new CodeableConcept(
                new Coding(STATUS_REASON, "PATCAR", "Patient care")).setText(reason));
        fhir.update(immunization, patientId);
        return summary(immunization);
    }

    private static ImmunizationSummary summary(Immunization immunization) {
        Coding vaccine = immunization.getVaccineCode().getCodingFirstRep();
        Integer dose = immunization.getProtocolApplied().isEmpty() ? null
                : immunization.getProtocolAppliedFirstRep().hasDoseNumberPositiveIntType()
                ? immunization.getProtocolAppliedFirstRep().getDoseNumberPositiveIntType().getValue() : null;
        return new ImmunizationSummary(
                immunization.getIdElement().getIdPart(),
                immunization.getPatient().getReferenceElement().getIdPart(),
                vaccine.getSystem(), vaccine.getCode(), vaccine.getDisplay(),
                immunization.getStatus() == null ? null : immunization.getStatus().toCode(),
                occurrence(immunization),
                immunization.hasLotNumber() ? immunization.getLotNumber() : null,
                immunization.hasSite() ? immunization.getSite().getText() : null,
                dose,
                immunization.hasStatusReason() ? immunization.getStatusReason().getText() : null);
    }

    private static LocalDate occurrence(Immunization immunization) {
        if (immunization.getOccurrence() instanceof DateTimeType date && date.getValue() != null) {
            return date.getValue().toInstant().atZone(ZoneOffset.UTC).toLocalDate();
        }
        return null;
    }
}
