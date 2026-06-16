package com.zantrix.iam;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that require explicit audit logging.
 * <p>
 * Any method annotated with {@code @AuditLoggable} will be intercepted by the {@link AuditAspect}
 * to generate a tamper-evident audit log entry. This is vital for meeting MDR/NEN7510 accountability rules.
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLoggable {
    /**
     * Specifies the name of the method parameter that contains the patient ID, 
     * which will be extracted and included in the audit log.
     *
     * @return The parameter name representing the patient ID.
     */
    String patientIdParam() default "";
}
