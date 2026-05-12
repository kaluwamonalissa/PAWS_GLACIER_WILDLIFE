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
public class WildlifeCategoryResponse {
    private String category;
    private List<String> species;
    private String riskLevel;
}

