/**
 * Patient administration and master patient index. Patient is the canonical
 * FHIR record; only reversible merge workflow metadata is stored relationally.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Patient and Master Patient Index",
        allowedDependencies = "platform::fhir")
package com.zantrix.patient;
