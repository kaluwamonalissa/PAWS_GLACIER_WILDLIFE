package za.co.mwm.paws.paws.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import za.co.mwm.paws.paws.domain.Patrol;

public interface PatrolRepository extends JpaRepository<Patrol, Long> {

    List<Patrol> findByRangerId(final Long rangerId);

    List<Patrol> findByWard(final String ward);
}

