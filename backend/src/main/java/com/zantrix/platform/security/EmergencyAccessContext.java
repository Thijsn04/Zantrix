package com.zantrix.platform.security;

import java.util.Optional;

/** Request-scoped, coded emergency access state. Free-text clinical detail is never stored here. */
public final class EmergencyAccessContext {

    private static final ThreadLocal<String> REASON = new ThreadLocal<>();

    private EmergencyAccessContext() { }

    public static void open(String reasonCode) { REASON.set(reasonCode); }
    public static boolean active() { return REASON.get() != null; }
    public static Optional<String> reasonCode() { return Optional.ofNullable(REASON.get()); }
    public static void clear() { REASON.remove(); }
}
