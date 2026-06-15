package com.zantrix.pmi.domain;

import ca.uhn.fhir.context.FhirContext;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.hl7.fhir.r4.model.Patient;

@Converter
public class FhirPatientConverter implements AttributeConverter<Patient, String> {

    private static final FhirContext FHIR_CONTEXT = FhirContext.forR4();

    @Override
    public String convertToDatabaseColumn(Patient patient) {
        if (patient == null) {
            return null;
        }
        return FHIR_CONTEXT.newJsonParser().encodeResourceToString(patient);
    }

    @Override
    public Patient convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        return FHIR_CONTEXT.newJsonParser().parseResource(Patient.class, dbData);
    }
}
