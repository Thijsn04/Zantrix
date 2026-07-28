package com.zantrix.platform;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The published API contract.
 *
 * The description is generated from the controllers rather than maintained by
 * hand, so it cannot describe behaviour the implementation does not have. The
 * frontend generates its types from it and fails its build when the two differ.
 * See <a href="../../../../../../docs/architecture/decisions/0011-generated-api-contract.md">ADR 0011</a>.
 *
 * <p>The document itself is not public. It is served from `/v3/api-docs` and,
 * like every other non-probe endpoint, requires authentication.
 */
@Configuration
public class OpenApiConfiguration {

    static final String BEARER_SCHEME = "bearer-jwt";

    @Bean
    public OpenAPI zantrixOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Zantrix Application API")
                        .description("""
                                Task oriented endpoints for the Zantrix clinical workspace and patient portal. \
                                Clinical state is FHIR R4 and is reached through the guarded gateway; these \
                                endpoints compose it and never form a parallel data model. The conformant FHIR \
                                surface is published separately at /fhir/R4.""")
                        .version("v1")
                        .license(new License()
                                .name("GNU Affero General Public License v3.0")
                                .url("https://www.gnu.org/licenses/agpl-3.0.txt")))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Keycloak issued access token carrying realm roles and SMART scopes.")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
