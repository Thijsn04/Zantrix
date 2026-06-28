package com.zantrix.referral;

import com.zantrix.iam.AuditLoggable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/referral")
public class ReferralController {

    private final ServiceRequestRepository repository;
    private final DocumentReferenceRepository documentReferenceRepository;

    public ReferralController(ServiceRequestRepository repository, DocumentReferenceRepository documentReferenceRepository) {
        this.repository = repository;
        this.documentReferenceRepository = documentReferenceRepository;
    }

    @GetMapping("/inbox")
    @PreAuthorize("hasAnyRole('PRACTITIONER', 'NURSE', 'SYSTEM_ADMIN')")
    @AuditLoggable
    public List<ServiceRequestEntity> getActiveReferrals() {
        return repository.findByStatus(ServiceRequestEntity.Status.ACTIVE);
    }

    @PostMapping("/receive")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')") // Only systems should be pushing here typically
    @AuditLoggable(patientIdParam = "patientId")
    public ServiceRequestEntity receiveReferral(@RequestBody ServiceRequestEntity request) {
        request.setStatus(ServiceRequestEntity.Status.ACTIVE);
        if (request.getAuthoredOn() == null) {
            request.setAuthoredOn(java.time.OffsetDateTime.now());
        }
        return repository.save(request);
    }

    @GetMapping("/external/{id}")
    @PreAuthorize("hasAnyRole('PRACTITIONER', 'NURSE')")
    @AuditLoggable(patientIdParam = "patientId")
    public java.util.Map<String, Object> getExternalReferral(@PathVariable UUID id) {
        ServiceRequestEntity request = repository.findById(id).orElseThrow();
        List<DocumentReferenceEntity> documents = documentReferenceRepository.findByServiceRequestId(id);
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("serviceRequest", request);
        result.put("documents", documents);
        
        return result;
    }
}
