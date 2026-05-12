package za.co.mwm.paws.paws.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatrolResponse {
    private Long id;
    private String rangerName;
    private Double startLatitude;
    private Double startLongitude;
    private Double endLatitude;
    private Double endLongitude;
    private Double distanceKm;
    private Integer snarersRemoved;
    private Integer wildlifeObserved;
    private String notes;
    private String ward;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
}

