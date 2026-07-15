package com.zantrix.terminology.internal;

import com.zantrix.terminology.CodeValidation;
import com.zantrix.terminology.TermConcept;
import com.zantrix.terminology.TerminologyValidator;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TerminologyService implements TerminologyValidator {

    private static final String SNOMED_CLINICAL_FINDING =
            "http://snomed.info/sct?fhir_vs=ecl/%3C%3C404684003";
    private static final String SNOMED_SUBSTANCE =
            "http://snomed.info/sct?fhir_vs=ecl/%3C%3C105590001";
    private static final String SNOMED_PROCEDURE =
            "http://snomed.info/sct?fhir_vs=ecl/%3C%3C71388002";
    private static final Map<String, String> SNOMED_DOMAINS = Map.of(
            "clinical-finding", SNOMED_CLINICAL_FINDING,
            "substance", SNOMED_SUBSTANCE,
            "procedure", SNOMED_PROCEDURE);

    private final SnowstormClient snowstorm;
    public TerminologyService(SnowstormClient snowstorm) { this.snowstorm = snowstorm; }

    public Map<String, Object> status() {
        Map<String, Object> serviceStatus = snowstorm.status();
        return Map.of("reachable", true, "fhirVersion", "4.0.1", "software", "Snowstorm",
                "version", String.valueOf(serviceStatus.getOrDefault("version", "unknown")));
    }

    public List<TermConcept> searchSnomed(String domain, String filter, int count, String language) {
        String valueSet = SNOMED_DOMAINS.get(domain);
        if (valueSet == null) {
            throw new IllegalArgumentException("domain must be clinical-finding, substance, or procedure");
        }
        return expand(valueSet, filter, count, language);
    }

    public List<TermConcept> expand(String valueSetUrl, String filter, int count, String language) {
        if (count < 1 || count > 100) {
            throw new IllegalArgumentException("count must be between 1 and 100");
        }
        ValueSet expansion = snowstorm.expand(valueSetUrl, filter, count, language);
        String version = expansion.getExpansion().getParameter().stream()
                .filter(parameter -> "version".equals(parameter.getName()))
                .map(parameter -> parameter.getValue() == null ? null : parameter.getValue().primitiveValue())
                .filter(java.util.Objects::nonNull).findFirst().orElse(null);
        return expansion.getExpansion().getContains().stream().map(item -> new TermConcept(
                item.getSystem(), item.getVersion() == null ? version : item.getVersion(), item.getCode(),
                item.getDisplay(), item.getInactive())).toList();
    }

    public CodeValidation validate(String system, String code, String display) {
        Parameters result = snowstorm.validateCode(system, code, display);
        return new CodeValidation(booleanParameter(result, "result"), stringParameter(result, "display"),
                stringParameter(result, "message"));
    }

    @Override
    public void requireValid(String system, String code, String display) {
        if (system == null || system.isBlank() || code == null || code.isBlank()) {
            throw new IllegalArgumentException("A coding system and code are required");
        }
        CodeValidation result = validate(system, code, display);
        if (!result.valid()) {
            throw new IllegalArgumentException("The clinical code is not valid in the active terminology edition");
        }
    }

    private static boolean booleanParameter(Parameters parameters, String name) {
        Parameters.ParametersParameterComponent value = parameter(parameters, name);
        return value != null && value.getValue() != null && Boolean.parseBoolean(value.getValue().primitiveValue());
    }

    private static String stringParameter(Parameters parameters, String name) {
        Parameters.ParametersParameterComponent value = parameter(parameters, name);
        return value == null || value.getValue() == null ? null : value.getValue().primitiveValue();
    }

    private static Parameters.ParametersParameterComponent parameter(Parameters parameters, String name) {
        return parameters.getParameter().stream().filter(value -> name.equals(value.getName())).findFirst().orElse(null);
    }
}
