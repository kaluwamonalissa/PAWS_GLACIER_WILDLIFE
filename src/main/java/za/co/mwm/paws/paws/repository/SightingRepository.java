package za.co.mwm.paws.paws.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.mwm.paws.paws.domain.Sighting;

public interface SightingRepository extends JpaRepository<Sighting, Long> {}

