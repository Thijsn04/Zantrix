package com.zantrix.interop;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface MessageLogRepository extends JpaRepository<MessageLogEntity, UUID> {
}
