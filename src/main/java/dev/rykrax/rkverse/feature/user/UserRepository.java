package dev.rykrax.rkverse.feature.user;

import dev.rykrax.rkverse.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<User> findByUsernameAndStatusNot(String username, UserStatus status);
}
