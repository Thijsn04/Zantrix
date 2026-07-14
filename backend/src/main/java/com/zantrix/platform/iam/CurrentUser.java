package com.zantrix.platform.iam;

import java.util.List;

/**
 * The authenticated user as seen by the frontend.
 *
 * @param subject     the stable identifier of the user (the token subject)
 * @param username    the preferred username
 * @param displayName the human readable name, if present
 * @param roles       realm roles granted to the user
 * @param scopes      OAuth2 and SMART on FHIR scopes granted to the session
 */
public record CurrentUser(
        String subject,
        String username,
        String displayName,
        List<String> roles,
        List<String> scopes) {
}
