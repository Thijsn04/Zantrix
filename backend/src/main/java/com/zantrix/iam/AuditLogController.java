package com.zantrix.iam;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/**
 * REST Controller for accessing system audit logs.
 * <p>
 * This endpoint is restricted to users with the 'PRIVACY_OFFICER' role. It provides paginated
 * access to the immutable audit logs to monitor system activity, investigate security incidents,
 * and fulfill compliance auditing under MDR and NEN7510 standards.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/iam/audit-logs")
class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    /**
     * Constructs a new {@link AuditLogController}.
     *
     * @param auditLogRepository The repository for audit logs.
     */
    public AuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Retrieves a paginated list of audit logs, sorted by timestamp descending by default.
     *
     * @param pageable The pagination and sorting parameters.
     * @return A map containing the paginated content and metadata.
     */
    @GetMapping
    @PreAuthorize("hasRole('PRIVACY_OFFICER')")
    public Map<String, Object> getAuditLogs(@PageableDefault(sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AuditLog> page = auditLogRepository.findAll(pageable);
        return Map.of(
            "content", page.getContent(),
            "totalPages", page.getTotalPages(),
            "totalElements", page.getTotalElements()
        );
    }
}
