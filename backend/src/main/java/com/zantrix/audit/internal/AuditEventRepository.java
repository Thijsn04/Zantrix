package com.zantrix.audit.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface AuditEventRepository extends JpaRepository<AuditEventEntity, Long> {

    List<AuditEventEntity> findAllByOrderByIdAsc();
}
