package org.solace.scholar_ai.user_service.repository;

import java.util.Optional;
import java.util.UUID;
import org.solace.scholar_ai.user_service.model.UserIdentityProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserIdentityProviderRepository extends JpaRepository<UserIdentityProvider, UUID> {
    UserIdentityProvider findByUserId(UUID userId);

    @Query("SELECT uip FROM UserIdentityProvider uip WHERE uip.user.email = :email")
    Optional<UserIdentityProvider> findByUserEmail(@Param("email") String email);

    @Query(
            "SELECT uip FROM UserIdentityProvider uip WHERE uip.provider = :provider AND uip.providerUserId = :providerUserId")
    Optional<UserIdentityProvider> findByProviderAndProviderUserId(
            @Param("provider") String provider, @Param("providerUserId") String providerUserId);

    @Query("SELECT uip FROM UserIdentityProvider uip WHERE uip.user.email = :email AND uip.provider = :provider")
    Optional<UserIdentityProvider> findByUserEmailAndProvider(
            @Param("email") String email, @Param("provider") String provider);
}
