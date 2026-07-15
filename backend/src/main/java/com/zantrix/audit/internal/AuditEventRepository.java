package com.zantrix.audit.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface AuditEventRepository extends JpaRepository<AuditEventEntity, Long> {

    List<AuditEventEntity> findAllByOrderByIdAsc();

    List<AuditEventEntity> findTop100ByExportStatusOrderByIdAsc(String exportStatus);

    @Query("""
            select e from AuditEventEntity e
            where (:patientId is null or e.patientId = :patientId)
              and (:actor is null or e.actor = :actor)
              and (:outcome is null or e.outcome = :outcome)
              and (:breakGlass is null or e.breakTheGlass = :breakGlass)
              and (:fromTime is null or e.recordedAt >= :fromTime)
              and (:toTime is null or e.recordedAt <= :toTime)
            order by e.recordedAt desc, e.id desc
            """)
    Page<AuditEventEntity> search(@Param("patientId") String patientId,@Param("actor") String actor,
                                  @Param("outcome") String outcome,@Param("breakGlass") Boolean breakGlass,
                                  @Param("fromTime") Instant from,@Param("toTime") Instant to,Pageable pageable);
}
