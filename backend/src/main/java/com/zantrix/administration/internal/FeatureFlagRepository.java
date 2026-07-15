package com.zantrix.administration.internal;

import org.springframework.data.jpa.repository.JpaRepository;

interface FeatureFlagRepository extends JpaRepository<FeatureFlagEntity, String> { }
