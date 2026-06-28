package com.zantrix.iam;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;

public interface PractitionerRepository extends JpaRepository<PractitionerEntity, UUID> {
    Optional<PractitionerEntity> findByKeycloakId(String keycloakId);
}
