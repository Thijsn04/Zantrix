package com.zantrix.iam;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/v1/practitioners")
public class PractitionerController {

    private final PractitionerRepository practitionerRepository;
    private final PractitionerRoleRepository roleRepository;

    public PractitionerController(PractitionerRepository practitionerRepository, PractitionerRoleRepository roleRepository) {
        this.practitionerRepository = practitionerRepository;
        this.roleRepository = roleRepository;
    }

    @GetMapping("/me")
    @AuditLoggable
    public Map<String, Object> getMe() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName(); // In reality, the Keycloak ID/subject

        PractitionerEntity practitioner = practitionerRepository.findByKeycloakId(username)
                .orElseGet(() -> {
                    // Auto-provision for simplicity in this system
                    return practitionerRepository.save(new PractitionerEntity(username, "BIG-" + username.hashCode(), true, "{}"));
                });

        List<PractitionerRoleEntity> roles = roleRepository.findByPractitionerId(practitioner.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("practitioner", practitioner);
        response.put("roles", roles);
        return response;
    }
}
