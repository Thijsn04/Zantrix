package com.zantrix.audit;

/**
 * Privacy-safe request context captured for durable audit reconciliation.
 *
 * @param actor stable authenticated subject, never a display name
 * @param sourceIp originating network address, if available
 */
public record AuditContext(String actor, String sourceIp) {

    public AuditContext {
        if (actor == null || actor.isBlank()) {
            throw new IllegalArgumentException("actor is required");
        }
    }
}
