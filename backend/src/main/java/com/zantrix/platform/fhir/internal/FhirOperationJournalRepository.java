package com.zantrix.platform.fhir.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface FhirOperationJournalRepository extends JpaRepository<FhirOperationJournalEntity, UUID> {

    List<FhirOperationJournalEntity> findTop100ByStateOrderByUpdatedAtAsc(FhirJournalState state);
}
