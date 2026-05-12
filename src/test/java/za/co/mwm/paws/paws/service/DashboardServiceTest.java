package za.co.mwm.paws.paws.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
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
import za.co.mwm.paws.paws.dto.DashboardSummaryResponse;
import za.co.mwm.paws.paws.repository.IncidentRepository;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private IncidentRepository incidentRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(incidentRepository);
    }

    @Test
    void givenIncidents_whenGetSummary_shouldReturnCorrectCounts() {
        final User reporter = buildUser();
        final Incident open = buildIncident(1L, IncidentStatus.RECEIVED, reporter, null);
        final Incident resolved = buildIncident(2L, IncidentStatus.RESOLVED, reporter,
                LocalDateTime.now().minusHours(2));

        when(incidentRepository.findAll()).thenReturn(List.of(open, resolved));
        when(incidentRepository.findByStatus(IncidentStatus.RECEIVED)).thenReturn(List.of(open));
        when(incidentRepository.findByStatus(IncidentStatus.RANGER_ASSIGNED)).thenReturn(List.of());

        final DashboardSummaryResponse summary = dashboardService.getSummary();

        assertThat(summary.getIncidentsThisWeek()).isEqualTo(2);
        assertThat(summary.getResolvedThisWeek()).isEqualTo(1);
        assertThat(summary.getOpenIncidents()).isEqualTo(1);
    }

    @Test
    void givenNoIncidents_whenGetSummary_shouldReturnZeros() {
        when(incidentRepository.findAll()).thenReturn(List.of());
        when(incidentRepository.findByStatus(IncidentStatus.RECEIVED)).thenReturn(List.of());
        when(incidentRepository.findByStatus(IncidentStatus.RANGER_ASSIGNED)).thenReturn(List.of());

        final DashboardSummaryResponse summary = dashboardService.getSummary();

        assertThat(summary.getIncidentsThisWeek()).isZero();
        assertThat(summary.getAvgResponseTimeHrs()).isZero();
    }

    @Test
    void givenIncidents_whenExportCsv_shouldReturnCsvWithHeader() {
        final User reporter = buildUser();
        when(incidentRepository.findAll()).thenReturn(
                List.of(buildIncident(1L, IncidentStatus.RECEIVED, reporter, null)));

        final String csv = dashboardService.exportCsv(null, null, null);

        assertThat(csv).startsWith("id,type,status");
        assertThat(csv).contains("POACHING");
    }

    @Test
    void givenWardFilter_whenExportCsv_shouldFilterByWard() {
        final User reporter = buildUser();
        final Incident wardA = buildIncident(1L, IncidentStatus.RECEIVED, reporter, null);
        wardA.setWard("Ward A");
        final Incident wardB = buildIncident(2L, IncidentStatus.RECEIVED, reporter, null);
        wardB.setWard("Ward B");
        when(incidentRepository.findAll()).thenReturn(List.of(wardA, wardB));

        final String csv = dashboardService.exportCsv("Ward A", null, null);

        assertThat(csv.lines().count()).isEqualTo(2); // header + 1 row
    }

    private User buildUser() {
        return User.builder().id(1L).username("ranger1").fullName("Ranger Blessing Dube")
                .role(Role.RANGER).active(true).build();
    }

    private Incident buildIncident(final Long id, final IncidentStatus status,
            final User reporter, final LocalDateTime resolvedAt) {
        return Incident.builder().id(id).type(IncidentType.POACHING).status(status)
                .description("Test").latitude(-18.9).longitude(26.4)
                .reportedBy(reporter).timestamp(LocalDateTime.now().minusHours(3))
                .resolvedAt(resolvedAt).build();
    }
}

