package com.zantrix.iam;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BreakTheGlassSessionRepository extends JpaRepository<BreakTheGlassSession, Integer> {
    List<BreakTheGlassSession> findByUsernameOrderByCreatedAtDesc(String username);
}
