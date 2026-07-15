package com.zantrix.audit;

import org.springframework.data.domain.Page;
import java.time.Instant;

public interface AuditQuery {
    Page<AuditRecord> search(String patientId, String actor, String outcome, Boolean breakTheGlass,
                             Instant from, Instant to, int page, int size);
}
