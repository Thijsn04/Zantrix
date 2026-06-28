package com.zantrix.terminology.repository;

import com.zantrix.terminology.domain.ValueSetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ValueSetRepository extends JpaRepository<ValueSetEntity, UUID> {
    Optional<ValueSetEntity> findByUrl(String url);
}
