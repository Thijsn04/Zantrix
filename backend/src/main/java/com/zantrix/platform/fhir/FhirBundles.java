package com.zantrix.platform.fhir;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;

import java.util.List;

/** Type-safe extraction helpers for search and history bundles. */
public final class FhirBundles {

    private FhirBundles() {
    }

    public static <T extends Resource> List<T> resources(Bundle bundle, Class<T> type) {
        return bundle.getEntry().stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(type::isInstance)
                .map(type::cast)
                .toList();
    }
}
