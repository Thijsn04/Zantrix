package com.zantrix.audit.internal;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface AuditChainHeadRepository extends JpaRepository<AuditChainHeadEntity, Long> {

    /**
     * Loads the chain head with a pessimistic write lock (SELECT ... FOR UPDATE),
     * serializing concurrent audit writes so the hash chain stays consistent.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from AuditChainHeadEntity h where h.id = :id")
    Optional<AuditChainHeadEntity> findByIdForUpdate(@Param("id") Long id);
}
