package za.co.mwm.paws.paws.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {
    private long incidentsThisWeek;
    private double avgResponseTimeHrs;
    private long openIncidents;
    private long resolvedThisWeek;
    private List<HeatmapPoint> hotspots;
}

