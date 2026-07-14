package com.zantrix.platform.iam;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Identity endpoints for the current session.
 *
 * <p>The frontend calls {@code /api/v1/iam/me} after login to learn who the user
 * is and what they may do, rather than decoding the token itself. Roles and
 * scopes are derived from the authorities that the central security layer
 * resolved, so this is the single source of truth for the session.
 */
@RestController
@RequestMapping("/api/v1/iam")
public class IamController {

    @GetMapping("/me")
    public CurrentUser me(@AuthenticationPrincipal Jwt jwt, Authentication authentication) {
        return new CurrentUser(
                jwt.getSubject(),
                jwt.getClaimAsString("preferred_username"),
                jwt.getClaimAsString("name"),
                authoritiesWithPrefix(authentication, "ROLE_"),
                authoritiesWithPrefix(authentication, "SCOPE_"));
    }

    private static List<String> authoritiesWithPrefix(Authentication authentication, String prefix) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith(prefix))
                .map(authority -> authority.substring(prefix.length()))
                .sorted()
                .toList();
    }
}
