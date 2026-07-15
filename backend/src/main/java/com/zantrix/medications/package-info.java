/** Medication prescribing and reconciliation over FHIR medication resources. */
@org.springframework.modulith.ApplicationModule(
        displayName = "Medications",
        allowedDependencies = {"platform::fhir", "cds"})
package com.zantrix.medications;
