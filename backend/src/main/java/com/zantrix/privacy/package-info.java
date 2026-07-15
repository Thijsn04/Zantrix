/** Patient consent, privacy controls, and emergency-access review. */
@org.springframework.modulith.ApplicationModule(
        displayName = "Consent and Privacy",
        allowedDependencies = {"platform::fhir", "platform::security"})
package com.zantrix.privacy;
