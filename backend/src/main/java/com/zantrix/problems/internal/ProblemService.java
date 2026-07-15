package com.zantrix.problems.internal;

import com.zantrix.platform.fhir.FhirAccessGateway;
import com.zantrix.platform.fhir.FhirBundles;
import com.zantrix.problems.ProblemRequest;
import com.zantrix.problems.ProblemSummary;
import com.zantrix.terminology.TerminologyValidator;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ProblemService {

    private static final String CONDITION_STATUS = "http://terminology.hl7.org/CodeSystem/condition-clinical";
    private final FhirAccessGateway fhir;
    private final TerminologyValidator terminology;

    public ProblemService(FhirAccessGateway fhir, TerminologyValidator terminology) {
        this.fhir = fhir;
        this.terminology = terminology;
    }

    public ProblemSummary add(ProblemRequest request) {
        terminology.requireValid(request.codeSystem(), request.code(), request.display());
        Condition condition = new Condition();
        condition.setId(UUID.randomUUID().toString());
        condition.setSubject(new Reference("Patient/" + request.patientId()));
        if (request.encounterId() != null && !request.encounterId().isBlank()) {
            condition.setEncounter(new Reference("Encounter/" + request.encounterId()));
        }
        condition.setCode(new CodeableConcept(new Coding(request.codeSystem(), request.code(), request.display())));
        condition.setClinicalStatus(status("active"));
        condition.setVerificationStatus(new CodeableConcept(new Coding(
                "http://terminology.hl7.org/CodeSystem/condition-ver-status", "confirmed", "Confirmed")));
        condition.addCategory(new CodeableConcept(new Coding(
                "http://terminology.hl7.org/CodeSystem/condition-category", "problem-list-item", "Problem List Item")));
        if (request.onsetDate() != null) {
            condition.setOnset(new DateTimeType(request.onsetDate().toString()));
        }
        if (request.note() != null && !request.note().isBlank()) {
            condition.addNote().setText(request.note());
        }
        condition.setRecordedDate(new Date());
        fhir.create(condition, request.patientId());
        return summary(condition);
    }

    public List<ProblemSummary> list(String patientId, boolean includeResolved) {
        Map<String, List<String>> parameters = includeResolved
                ? Map.of("patient", List.of(patientId), "_sort", List.of("-recorded-date"), "_count", List.of("200"))
                : Map.of("patient", List.of(patientId), "clinical-status", List.of("active,recurrence,relapse"),
                "_sort", List.of("-recorded-date"), "_count", List.of("200"));
        return FhirBundles.resources(fhir.search("Condition", parameters, patientId), Condition.class)
                .stream().map(ProblemService::summary).toList();
    }

    public ProblemSummary resolve(String id, String patientId, LocalDate resolvedDate) {
        Condition condition = fhir.read(Condition.class, id, patientId);
        if (!"active".equals(condition.getClinicalStatus().getCodingFirstRep().getCode())) {
            throw new IllegalArgumentException("Only an active problem can be resolved");
        }
        condition.setClinicalStatus(status("resolved"));
        condition.setAbatement(new DateTimeType((resolvedDate == null ? LocalDate.now() : resolvedDate).toString()));
        fhir.update(condition, patientId);
        return summary(condition);
    }

    private static CodeableConcept status(String code) {
        return new CodeableConcept(new Coding(CONDITION_STATUS, code,
                Character.toUpperCase(code.charAt(0)) + code.substring(1)));
    }

    private static ProblemSummary summary(Condition condition) {
        Coding coding = condition.getCode().getCodingFirstRep();
        return new ProblemSummary(condition.getIdElement().getIdPart(),
                condition.getSubject().getReferenceElement().getIdPart(), coding.getSystem(), coding.getCode(),
                coding.getDisplay(), condition.getClinicalStatus().getCodingFirstRep().getCode(),
                date(condition.getOnset()), date(condition.getAbatement()));
    }

    private static LocalDate date(org.hl7.fhir.r4.model.Type value) {
        if (value instanceof DateTimeType date && date.getValue() != null) {
            return date.getValue().toInstant().atZone(ZoneOffset.UTC).toLocalDate();
        }
        return null;
    }
}
