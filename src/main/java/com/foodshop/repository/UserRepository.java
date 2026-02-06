package com.foodshop.repository;

import com.foodshop.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for User entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * Finds a user by email address.
   *
   * @param email the email address
   * @return optional containing the user if found
   */
  Optional<User> findByEmail(String email);

  /**
   * Finds a user by OAuth provider and external ID.
   *
   * @param provider the OAuth provider
   * @param externalId the external ID from the provider
   * @return optional containing the user if found
   */
  @Query("SELECT u FROM User u WHERE u.provider = :provider AND u.externalId = :externalId")
  Optional<User> findByProviderAndExternalId(
      @Param("provider") User.OAuthProvider provider,
      @Param("externalId") String externalId);

  /**
   * Checks if a user with the given email exists.
   *
   * @param email the email address
   * @return true if user exists, false otherwise
   */
  boolean existsByEmail(String email);
}
