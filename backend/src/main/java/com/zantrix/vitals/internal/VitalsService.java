package com.zantrix.vitals.internal;

import com.zantrix.platform.fhir.FhirAccessGateway;
import com.zantrix.platform.fhir.FhirBundles;
import com.zantrix.vitals.VitalMeasurement;
import com.zantrix.vitals.VitalSummary;
import com.zantrix.vitals.VitalsRequest;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class VitalsService {

    private static final String LOINC = "http://loinc.org";
    private static final String UCUM = "http://unitsofmeasure.org";
    private static final Map<String, VitalDefinition> DEFINITIONS = definitions();
    private final FhirAccessGateway fhir;

    public VitalsService(FhirAccessGateway fhir) { this.fhir = fhir; }

    public List<VitalSummary> record(VitalsRequest request) {
        List<VitalMeasurement> measurements = new ArrayList<>(request.measurements());
        addCalculatedBmi(measurements);
        Bundle transaction = new Bundle().setType(Bundle.BundleType.TRANSACTION);
        List<Observation> observations = measurements.stream()
                .map(measurement -> observation(request, measurement))
                .toList();
        observations.forEach(observation -> transaction.addEntry().setResource(observation)
                .getRequest().setMethod(Bundle.HTTPVerb.PUT)
                .setUrl("Observation/" + observation.getIdElement().getIdPart()));
        fhir.transaction(transaction, request.patientId());
        return observations.stream().map(VitalsService::summary).toList();
    }

    public List<VitalSummary> list(String patientId, int count) {
        int safeCount = Math.max(1, Math.min(count, 500));
        return FhirBundles.resources(fhir.search("Observation", Map.of(
                "patient", List.of(patientId), "category", List.of("vital-signs"),
                "_sort", List.of("-date"), "_count", List.of(Integer.toString(safeCount))),
                patientId), Observation.class).stream().map(VitalsService::summary).toList();
    }

    private static Observation observation(VitalsRequest request, VitalMeasurement measurement) {
        VitalDefinition definition = DEFINITIONS.get(measurement.loincCode());
        if (definition == null) {
            throw new IllegalArgumentException("Unsupported vital LOINC code: " + measurement.loincCode());
        }
        if (!definition.unit().equals(measurement.unit())) {
            throw new IllegalArgumentException(definition.display() + " requires UCUM unit " + definition.unit());
        }
        Observation observation = new Observation();
        observation.setId(UUID.randomUUID().toString());
        observation.setStatus(Observation.ObservationStatus.FINAL);
        observation.addCategory(new CodeableConcept(new Coding(
                "http://terminology.hl7.org/CodeSystem/observation-category", "vital-signs", "Vital Signs")));
        observation.setCode(new CodeableConcept(new Coding(LOINC, measurement.loincCode(), definition.display())));
        observation.setSubject(new Reference("Patient/" + request.patientId()));
        observation.setEncounter(new Reference("Encounter/" + request.encounterId()));
        observation.addPerformer(new Reference("Practitioner/" + request.performerId()));
        observation.setEffective(new org.hl7.fhir.r4.model.DateTimeType(Date.from(request.observedAt())));
        observation.setValue(new Quantity().setValue(measurement.value()).setSystem(UCUM)
                .setCode(definition.unit()).setUnit(definition.unit()));
        if (measurement.note() != null && !measurement.note().isBlank()) {
            observation.addNote().setText(measurement.note());
        }
        return observation;
    }

    private static void addCalculatedBmi(List<VitalMeasurement> measurements) {
        if (measurements.stream().anyMatch(value -> "39156-5".equals(value.loincCode()))) {
            return;
        }
        BigDecimal heightCm = value(measurements, "8302-2");
        BigDecimal weightKg = value(measurements, "29463-7");
        if (heightCm == null || weightKg == null || heightCm.signum() <= 0 || weightKg.signum() <= 0) {
            return;
        }
        BigDecimal heightM = heightCm.movePointLeft(2);
        BigDecimal bmi = weightKg.divide(heightM.multiply(heightM), 1, RoundingMode.HALF_UP);
        measurements.add(new VitalMeasurement("39156-5", bmi, "kg/m2", "Calculated from height and weight"));
    }

    private static BigDecimal value(List<VitalMeasurement> measurements, String code) {
        return measurements.stream().filter(value -> code.equals(value.loincCode()))
                .map(VitalMeasurement::value).findFirst().orElse(null);
    }

    private static VitalSummary summary(Observation observation) {
        Quantity value = observation.getValueQuantity();
        Instant observed = observation.getEffectiveDateTimeType().getValue() == null ? null
                : observation.getEffectiveDateTimeType().getValue().toInstant();
        Coding code = observation.getCode().getCodingFirstRep();
        return new VitalSummary(observation.getIdElement().getIdPart(), code.getCode(), code.getDisplay(),
                value.getValue(), value.getCode(), observed);
    }

    private static Map<String, VitalDefinition> definitions() {
        Map<String, VitalDefinition> values = new LinkedHashMap<>();
        values.put("8480-6", new VitalDefinition("Systolic blood pressure", "mm[Hg]"));
        values.put("8462-4", new VitalDefinition("Diastolic blood pressure", "mm[Hg]"));
        values.put("8867-4", new VitalDefinition("Heart rate", "/min"));
        values.put("8310-5", new VitalDefinition("Body temperature", "Cel"));
        values.put("9279-1", new VitalDefinition("Respiratory rate", "/min"));
        values.put("2708-6", new VitalDefinition("Oxygen saturation", "%"));
        values.put("29463-7", new VitalDefinition("Body weight", "kg"));
        values.put("8302-2", new VitalDefinition("Body height", "cm"));
        values.put("39156-5", new VitalDefinition("Body mass index", "kg/m2"));
        return Map.copyOf(values);
    }

    private record VitalDefinition(String display, String unit) { }
}
