package za.co.mwm.paws.paws.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.mwm.paws.paws.domain.Incident;
import za.co.mwm.paws.paws.domain.IncidentResponder;
import za.co.mwm.paws.paws.domain.IncidentStatus;
import za.co.mwm.paws.paws.domain.IncidentType;
import za.co.mwm.paws.paws.domain.User;
import za.co.mwm.paws.paws.dto.HeatmapPoint;
import za.co.mwm.paws.paws.dto.IncidentRequest;
import za.co.mwm.paws.paws.dto.IncidentResponse;
import za.co.mwm.paws.paws.dto.IncidentUpdateRequest;
import za.co.mwm.paws.paws.repository.IncidentRepository;
import za.co.mwm.paws.paws.repository.IncidentResponderRepository;
import za.co.mwm.paws.paws.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private static final String ERROR_INCIDENT_NOT_FOUND = "Incident not found";
    private static final String ERROR_USER_NOT_FOUND = "User not found";

    private final IncidentRepository incidentRepository;
    private final IncidentResponderRepository incidentResponderRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public IncidentResponse createIncident(final IncidentRequest request, final String username) {
        final User reporter = findUser(username);

        final Incident incident = Incident.builder()
                .type(request.getType())
                .status(IncidentStatus.RECEIVED)
                .description(request.getDescription())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .ward(request.getWard())
                .species(request.getSpecies())
                .cropLivestockType(request.getCropLivestockType())
                .damageEstimate(request.getDamageEstimate())
                .reportedBy(reporter)
                .timestamp(LocalDateTime.now())
                .build();

        final Incident saved = incidentRepository.save(incident);
        final IncidentResponse response = toResponse(saved);
        notificationService.broadcastIncidentUpdate(response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> getAllIncidents(
            final String ward, final IncidentStatus status, final IncidentType type) {
        List<Incident> incidents;
        if (ward != null && status != null) {
            incidents = incidentRepository.findByWardAndStatus(ward, status);
        } else if (ward != null) {
            incidents = incidentRepository.findByWard(ward);
        } else if (status != null) {
            incidents = incidentRepository.findByStatus(status);
        } else if (type != null) {
            incidents = incidentRepository.findByType(type);
        } else {
            incidents = incidentRepository.findAll();
        }
        return incidents.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> getMyIncidents(final String username) {
        final User user = findUser(username);
        return incidentRepository.findByReportedById(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public IncidentResponse respondToIncident(final Long incidentId, final String rangerUsername) {
        final Incident incident = findIncident(incidentId);
        final User ranger = findUser(rangerUsername);

        incident.setStatus(IncidentStatus.RANGER_ASSIGNED);
        incident.setAssignedRanger(ranger);
        incidentRepository.save(incident);

        incidentResponderRepository.save(IncidentResponder.builder()
                .incident(incident).ranger(ranger).assignedAt(LocalDateTime.now()).build());

        final IncidentResponse response = toResponse(incident);
        notificationService.broadcastIncidentUpdate(response);
        return response;
    }

    @Transactional
    public IncidentResponse updateIncident(
            final Long incidentId, final IncidentUpdateRequest request, final String username) {
        final Incident incident = findIncident(incidentId);
        incident.setActionTaken(request.getActionTaken());
        incident.setDeterrentsUsed(request.getDeterrentsUsed());
        incident.setOutcome(request.getOutcome());
        if (request.getOutcome() != null && !request.getOutcome().isBlank()) {
            incident.setStatus(IncidentStatus.RESOLVED);
            incident.setResolvedAt(LocalDateTime.now());
        }
        incidentRepository.save(incident);
        final IncidentResponse response = toResponse(incident);
        notificationService.broadcastIncidentUpdate(response);
        return response;
    }

    @Transactional
    public IncidentResponse assignIncident(final Long incidentId, final Long rangerId) {
        final Incident incident = findIncident(incidentId);
        final User ranger = userRepository.findById(rangerId)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_USER_NOT_FOUND));
        incident.setAssignedRanger(ranger);
        incident.setStatus(IncidentStatus.RANGER_ASSIGNED);
        incidentRepository.save(incident);
        final IncidentResponse response = toResponse(incident);
        notificationService.broadcastIncidentUpdate(response);
        return response;
    }

    @Transactional
    public IncidentResponse flagIncident(final Long incidentId, final IncidentStatus flagStatus) {
        final Incident incident = findIncident(incidentId);
        incident.setStatus(flagStatus);
        incidentRepository.save(incident);
        return toResponse(incident);
    }

    @Transactional(readOnly = true)
    public List<HeatmapPoint> getHeatmap() {
        return incidentRepository.findAll().stream()
                .map(i -> HeatmapPoint.builder()
                        .latitude(i.getLatitude())
                        .longitude(i.getLongitude())
                        .type(i.getType())
                        .count(1L)
                        .build())
                .toList();
    }

    private Incident findIncident(final Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_INCIDENT_NOT_FOUND));
    }

    private User findUser(final String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_USER_NOT_FOUND));
    }

    private IncidentResponse toResponse(final Incident incident) {
        final List<String> responders = incidentResponderRepository
                .findByIncidentId(incident.getId()).stream()
                .map(r -> r.getRanger().getFullName())
                .toList();

        return IncidentResponse.builder()
                .id(incident.getId())
                .type(incident.getType())
                .status(incident.getStatus())
                .description(incident.getDescription())
                .latitude(incident.getLatitude())
                .longitude(incident.getLongitude())
                .ward(incident.getWard())
                .species(incident.getSpecies())
                .cropLivestockType(incident.getCropLivestockType())
                .damageEstimate(incident.getDamageEstimate())
                .actionTaken(incident.getActionTaken())
                .deterrentsUsed(incident.getDeterrentsUsed())
                .outcome(incident.getOutcome())
                .reportedBy(incident.getReportedBy().getFullName())
                .assignedRanger(incident.getAssignedRanger() != null
                        ? incident.getAssignedRanger().getFullName() : null)
                .timestamp(incident.getTimestamp())
                .resolvedAt(incident.getResolvedAt())
                .responders(responders)
                .build();
    }
}

