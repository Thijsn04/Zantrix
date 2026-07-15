package com.zantrix.platform.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
class EmergencyAccessFilter extends OncePerRequestFilter {

    private static final Set<String> REASONS = Set.of("immediate-threat", "unavailable-provider", "disaster");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!"true".equalsIgnoreCase(request.getHeader("X-Zantrix-Emergency-Access"))) {
            chain.doFilter(request, response);
            return;
        }
        String reason = request.getHeader("X-Zantrix-Emergency-Reason");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean clinician = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_PHYSICIAN") || authority.equals("ROLE_NURSE"));
        if (!clinician || !REASONS.contains(reason)) {
            throw new AccessDeniedException("Emergency access requires an authorized clinician and approved reason code");
        }
        try {
            EmergencyAccessContext.open(reason);
            chain.doFilter(request, response);
        } finally {
            EmergencyAccessContext.clear();
        }
    }
}
