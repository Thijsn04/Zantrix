package com.zantrix.iam;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing {@link BreakTheGlassSession} entities.
 * <p>
 * Provides operations to interact with the database for break-the-glass (emergency access) sessions,
 * supporting auditing and compliance with MDR/NEN7510 security standards by tracking emergency data access.
 * </p>
 */
@Repository
public interface BreakTheGlassSessionRepository extends JpaRepository<BreakTheGlassSession, Integer> {
    
    /**
     * Retrieves a list of break-the-glass sessions for a specific username, ordered by creation time descending.
     *
     * @param username The username of the practitioner who initiated the sessions.
     * @return A list of {@link BreakTheGlassSession} instances.
     */
    List<BreakTheGlassSession> findByUsernameOrderByCreatedAtDesc(String username);
}
