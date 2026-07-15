package com.zantrix.platform.fhir.internal;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.ValidationResult;
import com.zantrix.platform.fhir.FhirValidationException;
import org.hl7.fhir.common.hapi.validation.support.CommonCodeSystemsTerminologyService;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.SnapshotGeneratingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.stereotype.Component;

import java.util.List;

/** Validates writes against base R4 and every profile declared in resource meta. */
@Component
class FhirResourceValidator {
    private final FhirValidator validator;

    FhirResourceValidator(FhirContext context) {
        ValidationSupportChain chain = new ValidationSupportChain(
                new DefaultProfileValidationSupport(context),
                new CommonCodeSystemsTerminologyService(context),
                new InMemoryTerminologyServerValidationSupport(context),
                new SnapshotGeneratingValidationSupport(context));
        FhirInstanceValidator module = new FhirInstanceValidator(chain);
        this.validator = context.newValidator().registerValidatorModule(module);
    }

    void validate(IBaseResource resource) {
        ValidationResult result = validator.validateWithResult(resource);
        List<String> failures = result.getMessages().stream()
                .filter(message -> message.getSeverity() == ResultSeverityEnum.ERROR
                        || message.getSeverity() == ResultSeverityEnum.FATAL)
                .map(message -> message.getLocationString() + ": " + message.getMessage())
                .toList();
        if (!failures.isEmpty()) {
            throw new FhirValidationException(failures);
        }
    }
}
