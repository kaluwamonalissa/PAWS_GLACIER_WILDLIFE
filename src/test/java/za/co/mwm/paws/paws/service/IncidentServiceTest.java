package za.co.mwm.paws.paws.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.mwm.paws.paws.domain.Incident;
import za.co.mwm.paws.paws.domain.IncidentStatus;
import za.co.mwm.paws.paws.domain.IncidentType;
import za.co.mwm.paws.paws.domain.Role;
import za.co.mwm.paws.paws.domain.User;
import za.co.mwm.paws.paws.dto.HeatmapPoint;
import za.co.mwm.paws.paws.dto.IncidentRequest;
import za.co.mwm.paws.paws.dto.IncidentResponse;
import za.co.mwm.paws.paws.repository.IncidentRepository;
import za.co.mwm.paws.paws.repository.IncidentResponderRepository;
import za.co.mwm.paws.paws.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

    @Mock private IncidentRepository incidentRepository;
    @Mock private IncidentResponderRepository incidentResponderRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;

    private IncidentService incidentService;

    @BeforeEach
    void setUp() {
        incidentService = new IncidentService(incidentRepository, incidentResponderRepository,
                userRepository, notificationService);
    }

    @Test
    void givenValidRequest_andKnownUser_whenCreateIncident_shouldReturnIncidentResponse() {
        final User reporter = buildUser(1L, "ranger1", Role.RANGER);
        final IncidentRequest request = IncidentRequest.builder()
                .type(IncidentType.POACHING).description("Snare found")
                .latitude(-18.90).longitude(26.45).build();

        when(userRepository.findByUsername("ranger1")).thenReturn(Optional.of(reporter));
        when(incidentRepository.save(any())).thenAnswer(inv -> {
            final Incident i = inv.getArgument(0);
            i.setId(1L);
            return i;
        });
        when(incidentResponderRepository.findByIncidentId(1L)).thenReturn(List.of());

        final IncidentResponse response = incidentService.createIncident(request, "ranger1");

        assertThat(response.getType()).isEqualTo(IncidentType.POACHING);
        assertThat(response.getStatus()).isEqualTo(IncidentStatus.RECEIVED);
        verify(notificationService).broadcastIncidentUpdate(any());
    }

    @Test
    void givenUnknownUser_whenCreateIncident_shouldThrowIllegalArgumentException() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        final IncidentRequest request = IncidentRequest.builder()
                .type(IncidentType.VELD_FIRE).description("Fire").latitude(-18.9).longitude(26.4).build();

        assertThatThrownBy(() -> incidentService.createIncident(request, "ghost"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void givenExistingIncidents_whenGetAllIncidents_shouldReturnList() {
        final User reporter = buildUser(1L, "ranger1", Role.RANGER);
        when(incidentRepository.findAll()).thenReturn(List.of(buildIncident(1L, reporter)));
        when(incidentResponderRepository.findByIncidentId(1L)).thenReturn(List.of());

        final List<IncidentResponse> result = incidentService.getAllIncidents(null, null, null);

        assertThat(result).hasSize(1);
    }

    @Test
    void givenValidIncidentAndRanger_whenRespondToIncident_shouldSetStatusRangerAssigned() {
        final User ranger   = buildUser(2L, "ranger1", Role.RANGER);
        final User reporter = buildUser(3L, "rancher1", Role.RANCHER);
        final Incident incident = buildIncident(5L, reporter);

        when(incidentRepository.findById(5L)).thenReturn(Optional.of(incident));
        when(userRepository.findByUsername("ranger1")).thenReturn(Optional.of(ranger));
        when(incidentRepository.save(any())).thenReturn(incident);
        when(incidentResponderRepository.save(any())).thenReturn(null);
        when(incidentResponderRepository.findByIncidentId(5L)).thenReturn(List.of());

        final IncidentResponse response = incidentService.respondToIncident(5L, "ranger1");

        assertThat(response.getStatus()).isEqualTo(IncidentStatus.RANGER_ASSIGNED);
        verify(notificationService).broadcastIncidentUpdate(any());
    }

    @Test
    void givenIncidents_whenGetHeatmap_shouldReturnHeatmapPoints() {
        final User reporter = buildUser(1L, "ranger1", Role.RANGER);
        when(incidentRepository.findAll()).thenReturn(List.of(buildIncident(1L, reporter)));

        final List<HeatmapPoint> points = incidentService.getHeatmap();

        assertThat(points).hasSize(1);
        assertThat(points.get(0).getType()).isEqualTo(IncidentType.POACHING);
    }

    @Test
    void givenNonExistentIncident_whenRespondToIncident_shouldThrowIllegalArgumentException() {
        when(incidentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> incidentService.respondToIncident(99L, "ranger1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void givenValidUpdate_whenUpdateIncident_shouldSetActionTakenAndResolve() {
        final User reporter = buildUser(1L, "ranger1", Role.RANGER);
        final Incident incident = buildIncident(1L, reporter);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any())).thenReturn(incident);
        when(incidentResponderRepository.findByIncidentId(1L)).thenReturn(List.of());

        final za.co.mwm.paws.paws.dto.IncidentUpdateRequest updateRequest =
                za.co.mwm.paws.paws.dto.IncidentUpdateRequest.builder()
                        .actionTaken("Used chilli bombs").deterrentsUsed("Chilli fence")
                        .outcome("Herd moved away — resolved").build();

        final IncidentResponse response = incidentService.updateIncident(1L, updateRequest, "ranger1");

        assertThat(response.getStatus()).isEqualTo(IncidentStatus.RESOLVED);
        assertThat(response.getActionTaken()).isEqualTo("Used chilli bombs");
    }

    @Test
    void givenValidAssignment_whenAssignIncident_shouldSetRangerAndStatus() {
        final User reporter  = buildUser(1L, "rancher1", Role.RANCHER);
        final User ranger    = buildUser(2L, "ranger1", Role.RANGER);
        final Incident incident = buildIncident(1L, reporter);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(userRepository.findById(2L)).thenReturn(Optional.of(ranger));
        when(incidentRepository.save(any())).thenReturn(incident);
        when(incidentResponderRepository.findByIncidentId(1L)).thenReturn(List.of());

        final IncidentResponse response = incidentService.assignIncident(1L, 2L);

        assertThat(response.getStatus()).isEqualTo(IncidentStatus.RANGER_ASSIGNED);
    }

    @Test
    void givenValidFlag_whenFlagIncident_shouldUpdateStatus() {
        final User reporter = buildUser(1L, "ranger1", Role.RANGER);
        final Incident incident = buildIncident(1L, reporter);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any())).thenReturn(incident);
        when(incidentResponderRepository.findByIncidentId(1L)).thenReturn(List.of());

        final IncidentResponse response = incidentService.flagIncident(1L, IncidentStatus.FALSE_ALARM);

        assertThat(response.getStatus()).isEqualTo(IncidentStatus.FALSE_ALARM);
    }

    @Test
    void givenUsername_whenGetMyIncidents_shouldReturnOnlyUsersIncidents() {
        final User reporter = buildUser(1L, "rancher1", Role.RANCHER);
        when(userRepository.findByUsername("rancher1")).thenReturn(Optional.of(reporter));
        when(incidentRepository.findByReportedById(1L))
                .thenReturn(List.of(buildIncident(1L, reporter)));
        when(incidentResponderRepository.findByIncidentId(1L)).thenReturn(List.of());

        final List<IncidentResponse> result = incidentService.getMyIncidents("rancher1");

        assertThat(result).hasSize(1);
    }

    private User buildUser(final Long id, final String username, final Role role) {
        return User.builder().id(id).username(username).fullName("Test User")
                .role(role).active(true).build();
    }

    private Incident buildIncident(final Long id, final User reporter) {
        return Incident.builder().id(id).type(IncidentType.POACHING)
                .status(IncidentStatus.RECEIVED).description("Test incident")
                .latitude(-18.90).longitude(26.45).reportedBy(reporter)
                .timestamp(java.time.LocalDateTime.now()).build();
    }
}

