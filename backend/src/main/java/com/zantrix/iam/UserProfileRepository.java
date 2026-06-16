package com.zantrix.iam;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link UserProfile} entities.
 * <p>
 * Provides standard CRUD operations for user profiles. Used to retrieve and persist
 * non-critical user preferences like UI language and theme.
 * </p>
 */
@Repository
interface UserProfileRepository extends JpaRepository<UserProfile, String> {
}
