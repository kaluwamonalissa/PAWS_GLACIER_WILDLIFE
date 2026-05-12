package za.co.mwm.paws.paws.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.mwm.paws.paws.domain.Patrol;
import za.co.mwm.paws.paws.domain.User;
import za.co.mwm.paws.paws.dto.PatrolRequest;
import za.co.mwm.paws.paws.dto.PatrolResponse;
import za.co.mwm.paws.paws.repository.PatrolRepository;
import za.co.mwm.paws.paws.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class PatrolService {

    private static final String ERROR_USER_NOT_FOUND = "User not found";

    private final PatrolRepository patrolRepository;
    private final UserRepository userRepository;

    @Transactional
    public PatrolResponse logPatrol(final PatrolRequest request, final String username) {
        final User ranger = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_USER_NOT_FOUND));

        final Patrol patrol = Patrol.builder()
                .ranger(ranger)
                .startLatitude(request.getStartLatitude())
                .startLongitude(request.getStartLongitude())
                .endLatitude(request.getEndLatitude())
                .endLongitude(request.getEndLongitude())
                .distanceKm(request.getDistanceKm())
                .snarersRemoved(request.getSnarersRemoved())
                .wildlifeObserved(request.getWildlifeObserved())
                .notes(request.getNotes())
                .ward(request.getWard())
                .startedAt(LocalDateTime.now())
                .build();

        final Patrol saved = patrolRepository.save(patrol);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PatrolResponse> getAllPatrols() {
        return patrolRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PatrolResponse> getMyPatrols(final String username) {
        final User ranger = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_USER_NOT_FOUND));
        return patrolRepository.findByRangerId(ranger.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    private PatrolResponse toResponse(final Patrol patrol) {
        return PatrolResponse.builder()
                .id(patrol.getId())
                .rangerName(patrol.getRanger().getFullName())
                .startLatitude(patrol.getStartLatitude())
                .startLongitude(patrol.getStartLongitude())
                .endLatitude(patrol.getEndLatitude())
                .endLongitude(patrol.getEndLongitude())
                .distanceKm(patrol.getDistanceKm())
                .snarersRemoved(patrol.getSnarersRemoved())
                .wildlifeObserved(patrol.getWildlifeObserved())
                .notes(patrol.getNotes())
                .ward(patrol.getWard())
                .startedAt(patrol.getStartedAt())
                .endedAt(patrol.getEndedAt())
                .build();
    }
}

