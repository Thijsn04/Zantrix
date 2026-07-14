package com.zantrix.platform.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Builds Spring Security authorities from a Keycloak issued access token.
 *
 * <p>Two kinds of authority are produced:
 * <ul>
 *   <li>Realm roles from the {@code realm_access.roles} claim, as {@code ROLE_} authorities.</li>
 *   <li>OAuth2 and SMART on FHIR scopes from the {@code scope} or {@code scp} claim, as
 *       {@code SCOPE_} authorities. This lets SMART scopes such as
 *       {@code patient/Observation.read} drive authorization alongside roles.</li>
 * </ul>
 */
public class JwtAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";
    private static final String SCOPE_CLAIM = "scope";
    private static final String SCP_CLAIM = "scp";

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        for (String role : realmRoles(jwt)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }
        for (String scope : scopes(jwt)) {
            authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope));
        }
        return authorities;
    }

    private static Collection<String> realmRoles(Jwt jwt) {
        Object realmAccess = jwt.getClaims().get(REALM_ACCESS_CLAIM);
        if (!(realmAccess instanceof Map<?, ?> realmAccessMap)) {
            return List.of();
        }
        Object roles = realmAccessMap.get(ROLES_CLAIM);
        if (!(roles instanceof Collection<?> roleList)) {
            return List.of();
        }
        return roleList.stream().filter(Objects::nonNull).map(Object::toString).toList();
    }

    private static Collection<String> scopes(Jwt jwt) {
        Object scope = jwt.getClaims().get(SCOPE_CLAIM);
        if (scope instanceof String scopeString && !scopeString.isBlank()) {
            return Arrays.asList(scopeString.trim().split("\\s+"));
        }
        Object scp = jwt.getClaims().get(SCP_CLAIM);
        if (scp instanceof Collection<?> scpList) {
            return scpList.stream().filter(Objects::nonNull).map(Object::toString).toList();
        }
        return List.of();
    }
}
