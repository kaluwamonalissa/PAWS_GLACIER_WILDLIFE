package za.co.mwm.paws.paws.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import za.co.mwm.paws.paws.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(final String username);

    boolean existsByUsername(final String username);
}

