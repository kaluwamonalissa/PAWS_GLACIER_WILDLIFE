package za.co.mwm.paws.paws.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import za.co.mwm.paws.paws.domain.IncidentType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeatmapPoint {
    private Double latitude;
    private Double longitude;
    private IncidentType type;
    private Long count;
}

