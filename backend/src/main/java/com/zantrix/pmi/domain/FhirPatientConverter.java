package com.zantrix.pmi.domain;

import ca.uhn.fhir.context.FhirContext;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.hl7.fhir.r4.model.Patient;

/**
 * JPA AttributeConverter for converting between FHIR Patient resource and its database JSON representation.
 * Facilitates seamless integration with FHIR R4 standards for interoperability.
 * Crucial for secure and compliant health data exchange (MDR/NEN7510).
 */
@Converter
public class FhirPatientConverter implements AttributeConverter<Patient, String> {

    /** The FHIR context initialized for R4 standard. */
    private static final FhirContext FHIR_CONTEXT = FhirContext.forR4();

    /**
     * Converts a FHIR Patient object to a JSON string for database storage.
     *
     * @param patient the FHIR Patient resource
     * @return a JSON string representation of the patient, or null if patient is null
     */
    @Override
    public String convertToDatabaseColumn(Patient patient) {
        if (patient == null) {
            return null;
        }
        return FHIR_CONTEXT.newJsonParser().encodeResourceToString(patient);
    }

    /**
     * Converts a JSON string from the database back to a FHIR Patient object.
     *
     * @param dbData the JSON string from the database
     * @return the deserialized FHIR Patient resource, or null if dbData is null/empty
     */
    @Override
    public Patient convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        return FHIR_CONTEXT.newJsonParser().parseResource(Patient.class, dbData);
    }
}
