package com.zantrix.medications.internal;

import com.zantrix.cds.MedicationSafetyChecker;
import com.zantrix.cds.MedicationCodeValidator;
import com.zantrix.cds.SafetyAssessment;
import com.zantrix.cds.SafetyIssue;
import com.zantrix.medications.MedicationSafetyException;
import com.zantrix.medications.PrescriptionRequest;
import com.zantrix.medications.PrescriptionSummary;
import com.zantrix.medications.MedicationEventRequest;
import com.zantrix.medications.MedicationEventSummary;
import com.zantrix.medications.MedicationReconciliationRequest;
import com.zantrix.platform.fhir.FhirAccessGateway;
import com.zantrix.platform.fhir.FhirBundles;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DetectedIssue;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.Duration;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.MedicationAdministration;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.Instant;

@Service
public class MedicationService {

    private static final String RXNORM = "http://www.nlm.nih.gov/research/umls/rxnorm";
    private final FhirAccessGateway fhir;
    private final MedicationSafetyChecker safetyChecker;
    private final MedicationCodeValidator medicationCodes;

    public MedicationService(FhirAccessGateway fhir, MedicationSafetyChecker safetyChecker,
                             MedicationCodeValidator medicationCodes) {
        this.fhir = fhir;
        this.safetyChecker = safetyChecker;
        this.medicationCodes = medicationCodes;
    }

    public SafetyAssessment check(String patientId, String rxNormIngredientCode, String display) {
        medicationCodes.requireIngredient(rxNormIngredientCode, display);
        return safetyChecker.assess(patientId, rxNormIngredientCode, display);
    }

    public PrescriptionSummary prescribe(PrescriptionRequest request) {
        SafetyAssessment assessment = check(request.patientId(), request.rxNormIngredientCode(),
                request.medicationDisplay());
        if (assessment.blocksPrescribing()
                && (request.safetyOverrideReason() == null || request.safetyOverrideReason().isBlank())) {
            throw new MedicationSafetyException(assessment);
        }
        MedicationRequest medication = toFhir(request);
        Bundle transaction = new Bundle().setType(Bundle.BundleType.TRANSACTION);
        addPut(transaction, medication);
        if (!assessment.issues().isEmpty()) {
            for (SafetyIssue issue : assessment.issues()) {
                addPut(transaction, detectedIssue(request, medication, issue));
            }
        }
        fhir.transaction(transaction, request.patientId());
        return summary(medication, assessment);
    }

    public List<PrescriptionSummary> list(String patientId, boolean includeStopped) {
        Map<String, List<String>> parameters = includeStopped
                ? Map.of("patient", List.of(patientId), "_sort", List.of("-authoredon"), "_count", List.of("300"))
                : Map.of("patient", List.of(patientId), "status", List.of("active,on-hold"),
                "_sort", List.of("-authoredon"), "_count", List.of("300"));
        return FhirBundles.resources(fhir.search("MedicationRequest", parameters, patientId),
                        MedicationRequest.class).stream()
                .map(request -> summary(request, null)).toList();
    }

    public PrescriptionSummary stop(String id, String patientId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("A stop reason is required");
        }
        MedicationRequest medication = fhir.read(MedicationRequest.class, id, patientId);
        if (medication.getStatus() != MedicationRequest.MedicationRequestStatus.ACTIVE
                && medication.getStatus() != MedicationRequest.MedicationRequestStatus.ONHOLD) {
            throw new IllegalArgumentException("Only an active or on-hold prescription can be stopped");
        }
        medication.setStatus(MedicationRequest.MedicationRequestStatus.STOPPED);
        medication.setStatusReason(new CodeableConcept().setText(reason));
        fhir.update(medication, patientId);
        return summary(medication, null);
    }

    public MedicationEventSummary reconcile(MedicationReconciliationRequest request) {
        MedicationStatement statement = new MedicationStatement();
        statement.setId(UUID.randomUUID().toString());
        try {
            statement.setStatus(MedicationStatement.MedicationStatementStatus.fromCode(request.status()));
        } catch (org.hl7.fhir.exceptions.FHIRException exception) {
            throw new IllegalArgumentException("Unsupported medication statement status");
        }
        statement.setSubject(new Reference("Patient/" + request.patientId()));
        if (request.encounterId() != null && !request.encounterId().isBlank()) {
            statement.setContext(new Reference("Encounter/" + request.encounterId()));
        }
        statement.setMedication(new CodeableConcept(new Coding(RXNORM,
                request.rxNormIngredientCode(), request.medicationDisplay())));
        statement.setDateAsserted(new Date());
        statement.setInformationSource(new Reference("Practitioner/" + request.performerId())
                .setDisplay(request.informationSource()));
        if (request.dosageText() != null && !request.dosageText().isBlank()) {
            statement.addDosage().setText(request.dosageText());
        }
        fhir.create(statement, request.patientId());
        return new MedicationEventSummary(statement.getIdElement().getIdPart(), "MedicationStatement",
                request.patientId(), null, statement.getStatus().toCode(), Instant.now());
    }

    public MedicationEventSummary administer(MedicationEventRequest request) {
        MedicationRequest prescription = fhir.read(MedicationRequest.class, request.prescriptionId(), request.patientId());
        MedicationAdministration administration = new MedicationAdministration();
        administration.setId(UUID.randomUUID().toString());
        administration.setStatus(MedicationAdministration.MedicationAdministrationStatus.COMPLETED);
        administration.setSubject(new Reference("Patient/" + request.patientId()));
        administration.setRequest(new Reference("MedicationRequest/" + request.prescriptionId()));
        administration.setMedication(prescription.getMedicationCodeableConcept().copy());
        administration.setEffective(new org.hl7.fhir.r4.model.DateTimeType(Date.from(request.occurredAt())));
        administration.addPerformer().setActor(new Reference("Practitioner/" + request.performerId()));
        administration.getDosage().setDose(new Quantity().setValue(request.quantity()).setUnit(request.unit()));
        administration.getDosage().setText(request.note());
        if (request.encounterId() != null && !request.encounterId().isBlank()) {
            administration.setContext(new Reference("Encounter/" + request.encounterId()));
        }
        fhir.create(administration, request.patientId());
        return new MedicationEventSummary(administration.getIdElement().getIdPart(), "MedicationAdministration",
                request.patientId(), request.prescriptionId(), administration.getStatus().toCode(), request.occurredAt());
    }

    public MedicationEventSummary dispense(MedicationEventRequest request) {
        MedicationRequest prescription = fhir.read(MedicationRequest.class, request.prescriptionId(), request.patientId());
        MedicationDispense dispense = new MedicationDispense();
        dispense.setId(UUID.randomUUID().toString());
        dispense.setStatus(MedicationDispense.MedicationDispenseStatus.COMPLETED);
        dispense.setSubject(new Reference("Patient/" + request.patientId()));
        dispense.addAuthorizingPrescription(new Reference("MedicationRequest/" + request.prescriptionId()));
        dispense.setMedication(prescription.getMedicationCodeableConcept().copy());
        dispense.setWhenHandedOver(Date.from(request.occurredAt()));
        dispense.setQuantity(new Quantity().setValue(request.quantity()).setUnit(request.unit()));
        dispense.addPerformer().setActor(new Reference("Practitioner/" + request.performerId()));
        if (request.note() != null && !request.note().isBlank()) dispense.addNote().setText(request.note());
        if (request.encounterId() != null && !request.encounterId().isBlank()) {
            dispense.setContext(new Reference("Encounter/" + request.encounterId()));
        }
        fhir.create(dispense, request.patientId());
        return new MedicationEventSummary(dispense.getIdElement().getIdPart(), "MedicationDispense",
                request.patientId(), request.prescriptionId(), dispense.getStatus().toCode(), request.occurredAt());
    }

    private static MedicationRequest toFhir(PrescriptionRequest request) {
        MedicationRequest medication = new MedicationRequest();
        medication.setId(UUID.randomUUID().toString());
        medication.setStatus(MedicationRequest.MedicationRequestStatus.ACTIVE);
        medication.setIntent(MedicationRequest.MedicationRequestIntent.ORDER);
        medication.setSubject(new Reference("Patient/" + request.patientId()));
        medication.setEncounter(new Reference("Encounter/" + request.encounterId()));
        medication.setRequester(new Reference("Practitioner/" + request.requesterId()));
        medication.setAuthoredOn(new Date());
        medication.setMedication(new CodeableConcept(new Coding(RXNORM,
                request.rxNormIngredientCode(), request.medicationDisplay())));
        Dosage dosage = medication.addDosageInstruction();
        dosage.setText(request.dosageText());
        dosage.getTiming().getRepeat().setFrequency(request.frequencyPerDay()).setPeriod(1)
                .setPeriodUnit(org.hl7.fhir.r4.model.Timing.UnitsOfTime.D);
        dosage.addDoseAndRate().setDose(new Quantity().setValue(request.doseValue()).setUnit(request.doseUnit()));
        if (request.routeCode() != null && !request.routeCode().isBlank()) {
            dosage.setRoute(new CodeableConcept(new Coding(request.routeSystem(), request.routeCode(), request.routeDisplay())));
        }
        Duration supplyDuration = new Duration();
        supplyDuration.setValue(request.durationDays());
        supplyDuration.setUnit("days");
        supplyDuration.setSystem("http://unitsofmeasure.org");
        supplyDuration.setCode("d");
        medication.getDispenseRequest().setQuantity(new Quantity().setValue(request.dispenseQuantity()))
                .setNumberOfRepeatsAllowed(request.repeats())
                .setExpectedSupplyDuration(supplyDuration);
        if (request.reason() != null && !request.reason().isBlank()) {
            medication.addReasonCode(new CodeableConcept().setText(request.reason()));
        }
        return medication;
    }

    private static DetectedIssue detectedIssue(PrescriptionRequest request, MedicationRequest medication,
                                               SafetyIssue issue) {
        DetectedIssue detected = new DetectedIssue();
        detected.setId(UUID.randomUUID().toString());
        detected.setStatus(DetectedIssue.DetectedIssueStatus.FINAL);
        detected.setSeverity(DetectedIssue.DetectedIssueSeverity.HIGH);
        detected.setPatient(new Reference("Patient/" + request.patientId()));
        detected.setIdentified(new org.hl7.fhir.r4.model.DateTimeType(new Date()));
        detected.setCode(new CodeableConcept(new Coding("https://zantrix.org/codes/medication-safety",
                issue.ruleId(), "Medication safety issue")));
        detected.setDetail(issue.summary());
        detected.addImplicated(new Reference("MedicationRequest/" + medication.getIdElement().getIdPart()));
        if (issue.existingMedicationId() != null) {
            detected.addImplicated(new Reference("MedicationRequest/" + issue.existingMedicationId()));
        }
        if (request.safetyOverrideReason() != null && !request.safetyOverrideReason().isBlank()) {
            detected.addMitigation().setDate(new Date()).setAction(new CodeableConcept().setText(
                    "Clinician override: " + request.safetyOverrideReason()))
                    .setAuthor(new Reference("Practitioner/" + request.requesterId()));
        }
        return detected;
    }

    private static void addPut(Bundle bundle, org.hl7.fhir.r4.model.Resource resource) {
        bundle.addEntry().setResource(resource).getRequest().setMethod(Bundle.HTTPVerb.PUT)
                .setUrl(resource.fhirType() + "/" + resource.getIdElement().getIdPart());
    }

    private static PrescriptionSummary summary(MedicationRequest medication, SafetyAssessment safety) {
        Coding code = medication.getMedicationCodeableConcept().getCodingFirstRep();
        String dosage = medication.getDosageInstruction().isEmpty() ? null
                : medication.getDosageInstructionFirstRep().getText();
        return new PrescriptionSummary(medication.getIdElement().getIdPart(),
                medication.getSubject().getReferenceElement().getIdPart(), code.getCode(), code.getDisplay(),
                medication.getStatus().toCode(), dosage, safety);
    }
}
