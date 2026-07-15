package com.zantrix.audit;

import java.util.List;

public interface AuditExportSource {
    List<AuditRecord> pendingExport(int limit);
    void markExported(long id,String fhirAuditEventId);
}
