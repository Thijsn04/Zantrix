package com.zantrix.scheduling.domain;

import ca.uhn.fhir.context.FhirContext;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.hl7.fhir.r4.model.Encounter;

/**
 * JPA AttributeConverter for converting between FHIR Encounter resource and its database JSON representation.
 */
@Converter
public class FhirEncounterConverter implements AttributeConverter<Encounter, String> {

    private static final FhirContext FHIR_CONTEXT = FhirContext.forR4();

    @Override
    public String convertToDatabaseColumn(Encounter encounter) {
        if (encounter == null) {
            return null;
        }
        return FHIR_CONTEXT.newJsonParser().encodeResourceToString(encounter);
    }

    @Override
    public Encounter convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        return FHIR_CONTEXT.newJsonParser().parseResource(Encounter.class, dbData);
    }
}
