package com.zantrix.pmi.domain;

import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FhirPatientConverterTest {

    @Test
    void testConvertAndParse() {
        FhirPatientConverter converter = new FhirPatientConverter();

        Patient patient = new Patient();
        patient.addName(new HumanName().setFamily("Doe").addGiven("John"));

        String json = converter.convertToDatabaseColumn(patient);
        assertNotNull(json);
        assertTrue(json.contains("Doe"));
        assertTrue(json.contains("John"));

        Patient parsed = converter.convertToEntityAttribute(json);
        assertNotNull(parsed);
        assertEquals("Doe", parsed.getNameFirstRep().getFamily());
        assertEquals("John", parsed.getNameFirstRep().getGivenAsSingleString());
    }

    @Test
    void testNullValues() {
        FhirPatientConverter converter = new FhirPatientConverter();
        assertNull(converter.convertToDatabaseColumn(null));
        assertNull(converter.convertToEntityAttribute(null));
        assertNull(converter.convertToEntityAttribute(""));
    }
}
