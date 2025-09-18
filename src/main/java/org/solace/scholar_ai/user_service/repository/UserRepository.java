package org.solace.scholar_ai.user_service.repository;

import java.util.Optional;
import java.util.UUID;
import org.solace.scholar_ai.user_service.model.User;
import org.solace.scholar_ai.user_service.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.profile WHERE u.id = :id")
    Optional<User> findWithProfileById(UUID id);
    
    // Count users by role
    long countByRole(UserRole role);
}
