package za.co.mwm.paws.paws.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import za.co.mwm.paws.paws.domain.Incident;
import za.co.mwm.paws.paws.domain.IncidentStatus;
import za.co.mwm.paws.paws.domain.IncidentType;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findByStatusNot(final IncidentStatus status);

    List<Incident> findByWard(final String ward);

    List<Incident> findByStatus(final IncidentStatus status);

    List<Incident> findByType(final IncidentType type);

    List<Incident> findByWardAndStatus(final String ward, final IncidentStatus status);

    List<Incident> findByReportedById(final Long userId);

    long countByStatusNot(final IncidentStatus status);
}
