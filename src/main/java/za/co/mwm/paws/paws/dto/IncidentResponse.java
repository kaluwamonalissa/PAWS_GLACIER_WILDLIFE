package za.co.mwm.paws.paws.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import za.co.mwm.paws.paws.domain.IncidentStatus;
import za.co.mwm.paws.paws.domain.IncidentType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentResponse {
    private Long id;
    private IncidentType type;
    private IncidentStatus status;
    private String description;
    private Double latitude;
    private Double longitude;
    private String ward;
    private String species;
    private String cropLivestockType;
    private BigDecimal damageEstimate;
    private String actionTaken;
    private String deterrentsUsed;
    private String outcome;
    private String reportedBy;
    private String assignedRanger;
    private LocalDateTime timestamp;
    private LocalDateTime resolvedAt;
    private List<String> responders;
}
