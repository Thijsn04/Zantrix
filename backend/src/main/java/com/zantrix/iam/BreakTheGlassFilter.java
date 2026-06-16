package com.zantrix.iam;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Security filter that evaluates if the current user has an active break-the-glass session.
 * <p>
 * Intercepts requests to check the repository for an unexpired emergency session for the user.
 * If active, it temporarily grants the 'ROLE_EMERGENCY_ACCESS' authority to the security context,
 * ensuring the user has the required privileges to access restricted medical data during an emergency.
 * </p>
 */
@Component
public class BreakTheGlassFilter extends OncePerRequestFilter {

    private final BreakTheGlassSessionRepository sessionRepository;

    /**
     * Constructs a new {@link BreakTheGlassFilter}.
     *
     * @param sessionRepository The repository to check for active sessions.
     */
    public BreakTheGlassFilter(BreakTheGlassSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    /**
     * Filters incoming HTTP requests to append emergency access roles if an active session is found.
     *
     * @param request The HTTP request.
     * @param response The HTTP response.
     * @param filterChain The filter chain to proceed with.
     * @throws ServletException If a servlet error occurs.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            String username = jwtAuth.getName();
            
            List<BreakTheGlassSession> sessions = sessionRepository.findByUsernameOrderByCreatedAtDesc(username);
            if (!sessions.isEmpty()) {
                BreakTheGlassSession session = sessions.get(0);
                if (session.getExpiresAt().isAfter(LocalDateTime.now())) {
                    List<GrantedAuthority> updatedAuthorities = new ArrayList<>(jwtAuth.getAuthorities());
                    updatedAuthorities.add(new SimpleGrantedAuthority("ROLE_EMERGENCY_ACCESS"));
                    
                    JwtAuthenticationToken newAuth = new JwtAuthenticationToken(
                            jwtAuth.getToken(), updatedAuthorities, jwtAuth.getName()
                    );
                    SecurityContextHolder.getContext().setAuthentication(newAuth);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
