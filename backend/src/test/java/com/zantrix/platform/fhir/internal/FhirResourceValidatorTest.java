package com.zantrix.platform.fhir.internal;

import ca.uhn.fhir.context.FhirContext;
import com.zantrix.platform.fhir.FhirValidationException;
import org.hl7.fhir.r4.model.Observation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FhirResourceValidatorTest {
    private final FhirResourceValidator validator = new FhirResourceValidator(FhirContext.forR4());

    @Test
    void rejectsResourceMissingRequiredElements() {
        assertThatThrownBy(() -> validator.validate(new Observation()))
                .isInstanceOf(FhirValidationException.class);
    }
}
