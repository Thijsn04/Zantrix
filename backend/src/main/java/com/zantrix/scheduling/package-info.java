/** Conflict-safe outpatient scheduling over FHIR Schedule, Slot, and Appointment. */
@org.springframework.modulith.ApplicationModule(
        displayName = "Scheduling and Resource Management",
        allowedDependencies = {"platform::fhir", "terminology"})
package com.zantrix.scheduling;
