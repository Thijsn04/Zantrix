package com.zantrix.iam;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface UserProfileRepository extends JpaRepository<UserProfile, String> {
}
