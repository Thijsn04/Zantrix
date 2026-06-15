package com.zantrix.terminology.repository;

import com.zantrix.terminology.domain.ImportJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImportJobRepository extends JpaRepository<ImportJob, Long> {
    Optional<ImportJob> findByFileName(String fileName);
}
