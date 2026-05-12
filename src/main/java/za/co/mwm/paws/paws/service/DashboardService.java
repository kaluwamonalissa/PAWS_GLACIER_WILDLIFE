package za.co.mwm.paws.paws.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.mwm.paws.paws.domain.IncidentStatus;
import za.co.mwm.paws.paws.domain.IncidentType;
import za.co.mwm.paws.paws.dto.DashboardSummaryResponse;
import za.co.mwm.paws.paws.dto.HeatmapPoint;
import za.co.mwm.paws.paws.repository.IncidentRepository;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final IncidentRepository incidentRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        final LocalDateTime weekAgo = LocalDateTime.now().minus(7, ChronoUnit.DAYS);

        final long thisWeek = incidentRepository.findAll().stream()
                .filter(i -> i.getTimestamp().isAfter(weekAgo))
                .count();

        final long resolvedThisWeek = incidentRepository.findAll().stream()
                .filter(i -> i.getTimestamp().isAfter(weekAgo)
                        && IncidentStatus.RESOLVED.equals(i.getStatus()))
                .count();

        final long openCount = incidentRepository.findByStatus(IncidentStatus.RECEIVED).size()
                + incidentRepository.findByStatus(IncidentStatus.RANGER_ASSIGNED).size();

        final double avgResponseHrs = incidentRepository.findAll().stream()
                .filter(i -> i.getResolvedAt() != null)
                .mapToLong(i -> ChronoUnit.MINUTES.between(i.getTimestamp(), i.getResolvedAt()))
                .average()
                .orElse(0.0) / 60.0;

        final List<HeatmapPoint> hotspots = incidentRepository.findAll().stream()
                .filter(i -> !IncidentStatus.RESOLVED.equals(i.getStatus()))
                .map(i -> HeatmapPoint.builder()
                        .latitude(i.getLatitude())
                        .longitude(i.getLongitude())
                        .type(i.getType())
                        .count(1L)
                        .build())
                .toList();

        return DashboardSummaryResponse.builder()
                .incidentsThisWeek(thisWeek)
                .resolvedThisWeek(resolvedThisWeek)
                .openIncidents(openCount)
                .avgResponseTimeHrs(Math.round(avgResponseHrs * 10.0) / 10.0)
                .hotspots(hotspots)
                .build();
    }

    @Transactional(readOnly = true)
    public String exportCsv(final String ward, final IncidentStatus status,
            final IncidentType type) {
        final StringBuilder csv = new StringBuilder();
        csv.append("id,type,status,description,ward,species,latitude,longitude,reportedBy,assignedRanger,timestamp,resolvedAt\n");
        incidentRepository.findAll().stream()
                .filter(i -> ward == null || ward.equals(i.getWard()))
                .filter(i -> status == null || status.equals(i.getStatus()))
                .filter(i -> type == null || type.equals(i.getType()))
                .forEach(i -> csv.append(String.format("%d,%s,%s,\"%s\",%s,%s,%.6f,%.6f,%s,%s,%s,%s\n",
                        i.getId(), i.getType(), i.getStatus(),
                        i.getDescription().replace("\"", "'"),
                        i.getWard() != null ? i.getWard() : "",
                        i.getSpecies() != null ? i.getSpecies() : "",
                        i.getLatitude(), i.getLongitude(),
                        i.getReportedBy().getFullName(),
                        i.getAssignedRanger() != null ? i.getAssignedRanger().getFullName() : "",
                        i.getTimestamp(),
                        i.getResolvedAt() != null ? i.getResolvedAt() : "")));
        return csv.toString();
    }
}

