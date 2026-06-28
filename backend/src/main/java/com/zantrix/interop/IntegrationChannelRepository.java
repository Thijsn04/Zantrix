package com.zantrix.interop;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface IntegrationChannelRepository extends JpaRepository<IntegrationChannelEntity, UUID> {
}
