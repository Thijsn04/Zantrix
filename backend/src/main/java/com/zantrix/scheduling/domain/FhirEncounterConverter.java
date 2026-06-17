package com.zantrix.scheduling.domain;

import ca.uhn.fhir.context.FhirContext;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.hl7.fhir.r4.model.Encounter;

/**
 * JPA AttributeConverter for converting between FHIR Encounter resource and its database JSON representation.
 * <p>
 * Ensures that patient visit data can be stored securely and natively as JSONB in PostgreSQL
 * while being fully compatible with the HAPI FHIR R4 standard.
 * </p>
 */
@Converter
public class FhirEncounterConverter implements AttributeConverter<Encounter, String> {

    private static final FhirContext FHIR_CONTEXT = FhirContext.forR4();

    /**
     * Converts a FHIR Encounter object to a JSON string for database storage.
     *
     * @param encounter The FHIR Encounter resource.
     * @return A JSON string representation, or null if the input is null.
     */
    @Override
    public String convertToDatabaseColumn(Encounter encounter) {
        if (encounter == null) {
            return null;
        }
        return FHIR_CONTEXT.newJsonParser().encodeResourceToString(encounter);
    }

    /**
     * Converts a JSON string from the database back into a FHIR Encounter object.
     *
     * @param dbData The JSON string retrieved from the database.
     * @return The deserialized FHIR Encounter resource, or null if input is empty.
     */
    @Override
    public Encounter convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        return FHIR_CONTEXT.newJsonParser().parseResource(Encounter.class, dbData);
    }
}
