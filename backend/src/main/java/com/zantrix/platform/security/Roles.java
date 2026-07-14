package com.zantrix.platform.security;

/**
 * Canonical role names used across Zantrix.
 *
 * <p>These are region neutral clinical and administrative roles. They are the
 * roles defined in the identity provider and carried in the realm_access.roles
 * claim of the access token, and they are what authorization checks refer to.
 * Keeping them in one place avoids the earlier drift where code checked for
 * roles that the identity provider never granted.
 *
 * <p>Authorities are exposed to Spring Security with the {@code ROLE_} prefix,
 * so a role such as {@link #PHYSICIAN} is checked with {@code hasRole('PHYSICIAN')}.
 */
public final class Roles {

    public static final String PHYSICIAN = "PHYSICIAN";
    public static final String NURSE = "NURSE";
    public static final String PHARMACIST = "PHARMACIST";
    public static final String ADMIN = "ADMIN";
    public static final String PRIVACY_OFFICER = "PRIVACY_OFFICER";
    public static final String PATIENT = "PATIENT";

    private Roles() {
    }
}
