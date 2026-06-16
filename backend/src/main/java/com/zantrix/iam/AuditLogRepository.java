package com.zantrix.iam;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository interface for managing {@link AuditLog} entities.
 * <p>
 * Provides access to the immutable audit logs. It is essential for generating
 * compliance reports and for the internal cryptographic hashing mechanism to find the preceding log entry.
 * </p>
 */
@Repository
interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    /**
     * Retrieves the most recently created audit log entry, used to maintain the hash chain.
     *
     * @return An {@link Optional} containing the latest {@link AuditLog}, or empty if no logs exist.
     */
    Optional<AuditLog> findTopByOrderByIdDesc();
}
