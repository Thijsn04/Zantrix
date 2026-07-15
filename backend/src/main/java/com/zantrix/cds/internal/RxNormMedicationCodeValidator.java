package com.zantrix.cds.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zantrix.cds.MedicationCodeValidationException;
import com.zantrix.cds.MedicationCodeValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Privacy-safe RxNorm validation. Only terminology codes are sent to NLM. */
@Component
class RxNormMedicationCodeValidator implements MedicationCodeValidator {
    private static final Duration TTL = Duration.ofHours(24);
    private static final java.util.Set<String> INGREDIENT_TYPES = java.util.Set.of("IN", "PIN", "MIN");
    private final RestClient client;
    private final Map<String, CachedConcept> cache = new ConcurrentHashMap<>();

    RxNormMedicationCodeValidator(RestClient.Builder builder,
                                  @Value("${zantrix.medication.rxnorm-base-url}") String baseUrl) {
        this.client = builder.baseUrl(baseUrl).build();
    }

    @Override
    public void requireIngredient(String rxCui, String expectedDisplay) {
        if (rxCui == null || !rxCui.matches("[0-9]{1,12}")) {
            throw new MedicationCodeValidationException("A numeric RxNorm ingredient code is required", false, null);
        }
        CachedConcept cached = cache.get(rxCui);
        if (cached != null && cached.checkedAt().plus(TTL).isAfter(Instant.now())) {
            requireIngredientType(cached.properties());
            return;
        }
        try {
            PropertiesEnvelope envelope = client.get().uri("/rxcui/{rxcui}/properties.json", rxCui)
                    .retrieve().body(PropertiesEnvelope.class);
            ConceptProperties properties = envelope == null ? null : envelope.properties();
            cache.put(rxCui, new CachedConcept(properties, Instant.now()));
            requireIngredientType(properties);
        } catch (MedicationCodeValidationException exception) {
            throw exception;
        } catch (RuntimeException failure) {
            throw new MedicationCodeValidationException(
                    "RxNorm validation is unavailable; prescribing is blocked until it recovers", true, failure);
        }
    }

    private static void requireIngredientType(ConceptProperties properties) {
        if (properties == null || !INGREDIENT_TYPES.contains(properties.tty())) {
            throw new MedicationCodeValidationException(
                    "The RxNorm code is not a current ingredient concept", false, null);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PropertiesEnvelope(ConceptProperties properties) { }
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ConceptProperties(String rxcui, String name, String tty) { }
    private record CachedConcept(ConceptProperties properties, Instant checkedAt) { }
}
