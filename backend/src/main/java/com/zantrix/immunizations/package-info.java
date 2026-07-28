/** Vaccination history over FHIR Immunization. */
@org.springframework.modulith.ApplicationModule(
        displayName = "Immunizations",
        allowedDependencies = {"platform::fhir", "terminology"})
package com.zantrix.immunizations;
