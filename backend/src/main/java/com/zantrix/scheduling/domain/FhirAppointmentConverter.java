package com.zantrix.scheduling.domain;

import ca.uhn.fhir.context.FhirContext;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.hl7.fhir.r4.model.Appointment;

/**
 * JPA AttributeConverter for converting between FHIR Appointment resource and its database JSON representation.
 * <p>
 * Ensures that complex medical scheduling data can be natively stored and queried as JSONB
 * in PostgreSQL while seamlessly integrating with the HAPI FHIR R4 library in Java.
 * </p>
 */
@Converter
public class FhirAppointmentConverter implements AttributeConverter<Appointment, String> {

    private static final FhirContext FHIR_CONTEXT = FhirContext.forR4();

    /**
     * Converts a FHIR Appointment object to a JSON string for database storage.
     *
     * @param appointment The FHIR Appointment resource.
     * @return A JSON string representation, or null if the input is null.
     */
    @Override
    public String convertToDatabaseColumn(Appointment appointment) {
        if (appointment == null) {
            return null;
        }
        return FHIR_CONTEXT.newJsonParser().encodeResourceToString(appointment);
    }

    /**
     * Converts a JSON string from the database back into a FHIR Appointment object.
     *
     * @param dbData The JSON string retrieved from the database.
     * @return The deserialized FHIR Appointment resource, or null if input is empty.
     */
    @Override
    public Appointment convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        return FHIR_CONTEXT.newJsonParser().parseResource(Appointment.class, dbData);
    }
}
