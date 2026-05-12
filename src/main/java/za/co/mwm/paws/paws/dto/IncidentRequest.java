package za.co.mwm.paws.paws.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import za.co.mwm.paws.paws.domain.IncidentType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentRequest {

    @NotNull
    private IncidentType type;

    @NotBlank
    private String description;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    private String ward;
    private String species;
    private String cropLivestockType;
    private BigDecimal damageEstimate;
}
