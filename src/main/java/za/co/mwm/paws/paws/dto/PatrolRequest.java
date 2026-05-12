package za.co.mwm.paws.paws.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatrolRequest {

    @NotNull
    private Double startLatitude;

    @NotNull
    private Double startLongitude;

    private Double endLatitude;
    private Double endLongitude;
    private Double distanceKm;
    private Integer snarersRemoved;
    private Integer wildlifeObserved;
    private String notes;
    private String ward;
}

