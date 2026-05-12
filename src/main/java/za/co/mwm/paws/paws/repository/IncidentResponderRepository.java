package za.co.mwm.paws.paws.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import za.co.mwm.paws.paws.domain.IncidentResponder;

public interface IncidentResponderRepository extends JpaRepository<IncidentResponder, Long> {

    List<IncidentResponder> findByIncidentId(final Long incidentId);

    List<IncidentResponder> findByRangerId(final Long rangerId);
}

