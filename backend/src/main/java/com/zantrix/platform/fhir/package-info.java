/**
 * Published FHIR access contract for application modules.
 *
 * <p>The guarded gateway is exposed as a named Spring Modulith interface. Its
 * transport and policy implementations remain internal to the platform module.
 */
@org.springframework.modulith.NamedInterface("fhir")
package com.zantrix.platform.fhir;
