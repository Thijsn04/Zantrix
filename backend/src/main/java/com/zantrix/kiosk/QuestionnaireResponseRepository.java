package com.zantrix.kiosk;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface QuestionnaireResponseRepository extends JpaRepository<QuestionnaireResponseEntity, UUID> {
}
