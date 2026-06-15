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

@Component
public class BreakTheGlassFilter extends OncePerRequestFilter {

    private final BreakTheGlassSessionRepository sessionRepository;

    public BreakTheGlassFilter(BreakTheGlassSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

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
