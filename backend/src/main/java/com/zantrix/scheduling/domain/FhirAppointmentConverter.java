package com.zantrix.scheduling.domain;

import ca.uhn.fhir.context.FhirContext;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.hl7.fhir.r4.model.Appointment;

/**
 * JPA AttributeConverter for converting between FHIR Appointment resource and its database JSON representation.
 */
@Converter
public class FhirAppointmentConverter implements AttributeConverter<Appointment, String> {

    private static final FhirContext FHIR_CONTEXT = FhirContext.forR4();

    @Override
    public String convertToDatabaseColumn(Appointment appointment) {
        if (appointment == null) {
            return null;
        }
        return FHIR_CONTEXT.newJsonParser().encodeResourceToString(appointment);
    }

    @Override
    public Appointment convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        return FHIR_CONTEXT.newJsonParser().parseResource(Appointment.class, dbData);
    }
}
