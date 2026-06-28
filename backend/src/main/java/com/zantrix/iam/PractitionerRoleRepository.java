package com.zantrix.iam;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface PractitionerRoleRepository extends JpaRepository<PractitionerRoleEntity, UUID> {
    List<PractitionerRoleEntity> findByPractitionerId(UUID practitionerId);
}
