package com.zantrix.charting.repository;

import com.zantrix.charting.domain.SmartTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SmartTemplateRepository extends JpaRepository<SmartTemplateEntity, UUID> {
    List<SmartTemplateEntity> findByAuthorIdOrAuthorIdIsNull(UUID authorId);
    SmartTemplateEntity findByShortcut(String shortcut);
}
