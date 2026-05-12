package za.co.mwm.paws.paws.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.mwm.paws.paws.domain.Patrol;
import za.co.mwm.paws.paws.domain.Role;
import za.co.mwm.paws.paws.domain.User;
import za.co.mwm.paws.paws.dto.PatrolRequest;
import za.co.mwm.paws.paws.dto.PatrolResponse;
import za.co.mwm.paws.paws.repository.PatrolRepository;
import za.co.mwm.paws.paws.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PatrolServiceTest {

    @Mock private PatrolRepository patrolRepository;
    @Mock private UserRepository userRepository;

    private PatrolService patrolService;

    @BeforeEach
    void setUp() {
        patrolService = new PatrolService(patrolRepository, userRepository);
    }

    @Test
    void givenValidRequest_andKnownRanger_whenLogPatrol_shouldReturnPatrolResponse() {
        final User ranger = buildRanger();
        final PatrolRequest request = PatrolRequest.builder()
                .startLatitude(-18.92).startLongitude(26.46)
                .distanceKm(4.2).snarersRemoved(3).wildlifeObserved(12)
                .notes("Patrol notes").ward("Ward 1 — Dete").build();

        when(userRepository.findByUsername("ranger1")).thenReturn(Optional.of(ranger));
        when(patrolRepository.save(any())).thenAnswer(inv -> {
            final Patrol p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        final PatrolResponse response = patrolService.logPatrol(request, "ranger1");

        assertThat(response.getRangerName()).isEqualTo("Ranger Blessing Dube");
        assertThat(response.getSnarersRemoved()).isEqualTo(3);
        assertThat(response.getWard()).isEqualTo("Ward 1 — Dete");
    }

    @Test
    void givenUnknownRanger_whenLogPatrol_shouldThrowIllegalArgumentException() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        final PatrolRequest request = PatrolRequest.builder()
                .startLatitude(-18.92).startLongitude(26.46).build();

        assertThatThrownBy(() -> patrolService.logPatrol(request, "ghost"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void givenPatrols_whenGetAllPatrols_shouldReturnList() {
        final User ranger = buildRanger();
        when(patrolRepository.findAll()).thenReturn(List.of(buildPatrol(ranger)));

        final List<PatrolResponse> result = patrolService.getAllPatrols();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDistanceKm()).isEqualTo(4.2);
    }

    @Test
    void givenKnownRanger_whenGetMyPatrols_shouldReturnOnlyTheirPatrols() {
        final User ranger = buildRanger();
        when(userRepository.findByUsername("ranger1")).thenReturn(Optional.of(ranger));
        when(patrolRepository.findByRangerId(1L)).thenReturn(List.of(buildPatrol(ranger)));

        final List<PatrolResponse> result = patrolService.getMyPatrols("ranger1");

        assertThat(result).hasSize(1);
    }

    @Test
    void givenUnknownRanger_whenGetMyPatrols_shouldThrowIllegalArgumentException() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> patrolService.getMyPatrols("ghost"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private User buildRanger() {
        return User.builder().id(1L).username("ranger1")
                .fullName("Ranger Blessing Dube").role(Role.RANGER).active(true).build();
    }

    private Patrol buildPatrol(final User ranger) {
        return Patrol.builder().id(1L).ranger(ranger)
                .startLatitude(-18.92).startLongitude(26.46)
                .distanceKm(4.2).snarersRemoved(3).wildlifeObserved(12)
                .notes("Test patrol").ward("Ward 1")
                .startedAt(java.time.LocalDateTime.now()).build();
    }
}

