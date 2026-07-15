package com.zantrix.patient.internal;

import com.zantrix.patient.PatientRegistration;
import com.zantrix.patient.PatientSummary;
import com.zantrix.patient.PotentialDuplicateException;
import com.zantrix.platform.fhir.FhirAccessGateway;
import com.zantrix.platform.fhir.FhirBundles;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PatientService {

    private static final double REVIEW_THRESHOLD = 0.65;
    private final FhirAccessGateway fhir;
    private final DuplicateScorer scorer = new DuplicateScorer();

    public PatientService(FhirAccessGateway fhir) {
        this.fhir = fhir;
    }

    public PatientSummary register(PatientRegistration request) {
        List<PatientSummary> duplicates = findDuplicates(request);
        if (!request.confirmedUnique() && !duplicates.isEmpty()) {
            throw new PotentialDuplicateException(duplicates);
        }
        Patient patient = PatientMapper.toFhir(request);
        fhir.create(patient, patient.getIdElement().getIdPart());
        return PatientMapper.summary(patient, 0);
    }

    public PatientSummary get(String id) {
        return PatientMapper.summary(fhir.read(Patient.class, id, id), 0);
    }

    public List<PatientSummary> search(String query, String birthDate, String identifier) {
        Map<String, List<String>> parameters = new LinkedHashMap<>();
        parameters.put("_count", List.of("50"));
        parameters.put("active", List.of("true"));
        if (query != null && !query.isBlank()) {
            parameters.put("name", List.of(query));
        }
        if (birthDate != null && !birthDate.isBlank()) {
            parameters.put("birthdate", List.of(birthDate));
        }
        if (identifier != null && !identifier.isBlank()) {
            parameters.put("identifier", List.of(identifier));
        }
        Bundle result = fhir.search("Patient", parameters, null);
        return FhirBundles.resources(result, Patient.class).stream()
                .map(patient -> PatientMapper.summary(patient, 0))
                .toList();
    }

    public List<PatientSummary> findDuplicates(PatientRegistration request) {
        Map<String, Patient> candidates = new LinkedHashMap<>();
        collect(candidates, Map.of("birthdate", List.of(request.birthDate().toString()),
                "family", List.of(request.familyName()), "_count", List.of("100")));
        if (request.identifierSystem() != null && !request.identifierSystem().isBlank()
                && request.identifierValue() != null && !request.identifierValue().isBlank()) {
            collect(candidates, Map.of("identifier", List.of(
                    request.identifierSystem() + "|" + request.identifierValue()), "_count", List.of("20")));
        }
        List<PatientSummary> result = new ArrayList<>();
        candidates.values().forEach(patient -> {
            double score = scorer.score(request, patient);
            if (score >= REVIEW_THRESHOLD) {
                result.add(PatientMapper.summary(patient, score));
            }
        });
        result.sort(java.util.Comparator.comparingDouble(PatientSummary::duplicateScore).reversed());
        return List.copyOf(result);
    }

    private void collect(Map<String, Patient> candidates, Map<String, List<String>> parameters) {
        FhirBundles.resources(fhir.search("Patient", parameters, null), Patient.class)
                .forEach(patient -> candidates.put(patient.getIdElement().getIdPart(), patient));
    }
}
