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
public class ModelMetricsResponse {
    private double auc;
    private double precisionAtK;
    private String lastRetrained;
    private String modelVersion;
    private List<WardRiskScore> wardRiskScores;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WardRiskScore {
        private String ward;
        private double riskScore;
        private String riskLevel;
    }
}

