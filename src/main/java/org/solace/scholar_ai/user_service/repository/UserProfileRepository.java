package org.solace.scholar_ai.user_service.repository;

import java.util.UUID;
import org.solace.scholar_ai.user_service.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    UserProfile findByUserId(UUID userId);

    UserProfile findByUserEmail(String email);
}
