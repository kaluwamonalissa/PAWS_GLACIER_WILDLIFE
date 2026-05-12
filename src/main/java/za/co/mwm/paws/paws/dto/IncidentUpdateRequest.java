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
public class IncidentUpdateRequest {

    @NotNull
    private String actionTaken;

    private String deterrentsUsed;
    private String outcome;
}

