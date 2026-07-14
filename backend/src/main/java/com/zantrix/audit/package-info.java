/**
 * Audit module: a tamper evident, privacy safe record of access and change.
 *
 * <p>Other modules record audit entries through {@link com.zantrix.audit.AuditRecorder}.
 * Entries are append only and are chained with a cryptographic hash so that
 * deletion or alteration is detectable. Entries hold identifiers, codes, and
 * references only. They never contain protected health information such as
 * names or clinical detail. See {@code docs/architecture/security-and-privacy.md}.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Audit")
package com.zantrix.audit;
