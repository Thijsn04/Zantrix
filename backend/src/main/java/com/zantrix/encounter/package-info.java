/** Outpatient encounter lifecycle over the canonical FHIR Encounter resource. */
@org.springframework.modulith.ApplicationModule(
        displayName = "Encounters and ADT",
        allowedDependencies = {"platform::fhir", "terminology"})
package com.zantrix.encounter;
