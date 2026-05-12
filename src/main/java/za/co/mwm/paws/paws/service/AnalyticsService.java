package za.co.mwm.paws.paws.service;

import java.util.List;
import org.springframework.stereotype.Service;
import za.co.mwm.paws.paws.dto.ModelMetricsResponse;
import za.co.mwm.paws.paws.dto.ModelMetricsResponse.WardRiskScore;
import za.co.mwm.paws.paws.dto.WhatIfRequest;
import za.co.mwm.paws.paws.dto.WhatIfResponse;

@Service
public class AnalyticsService {

    private static final double STUBBED_AUC = 0.87;
    private static final double STUBBED_PRECISION_AT_K = 0.74;
    private static final String STUBBED_LAST_RETRAINED = "2026-05-03T01:00:00";
    private static final String STUBBED_MODEL_VERSION = "hwange-hwc-v2.1";
    private static final double RESPONSE_TIME_REDUCTION_PER_RANGER = 0.18;

    public ModelMetricsResponse getModelMetrics() {
        return ModelMetricsResponse.builder()
                .auc(STUBBED_AUC)
                .precisionAtK(STUBBED_PRECISION_AT_K)
                .lastRetrained(STUBBED_LAST_RETRAINED)
                .modelVersion(STUBBED_MODEL_VERSION)
                .wardRiskScores(stubbedWardRiskScores())
                .build();
    }

    public WhatIfResponse simulateWhatIf(final WhatIfRequest request) {
        final double currentAvg = 2.8;
        final double projected = Math.max(
                0.5, currentAvg * (1.0 - RESPONSE_TIME_REDUCTION_PER_RANGER * request.getAdditionalRangers()));
        final double improvement = ((currentAvg - projected) / currentAvg) * 100.0;

        return WhatIfResponse.builder()
                .ward(request.getWard())
                .additionalRangers(request.getAdditionalRangers())
                .currentAvgResponseTimeHrs(currentAvg)
                .projectedAvgResponseTimeHrs(Math.round(projected * 10.0) / 10.0)
                .improvementPct(Math.round(improvement * 10.0) / 10.0)
                .recommendation(improvement > 30
                        ? "High impact — strongly recommended"
                        : "Moderate impact — consider alongside other wards")
                .build();
    }

    private List<WardRiskScore> stubbedWardRiskScores() {
        return List.of(
                wardRisk("Ward 1 — Dete", 0.91, "HIGH"),
                wardRisk("Ward 2 — Hwange Town", 0.73, "HIGH"),
                wardRisk("Ward 3 — Ngamo", 0.65, "MEDIUM"),
                wardRisk("Ward 4 — Siabuwa", 0.58, "MEDIUM"),
                wardRisk("Ward 5 — Binga", 0.42, "LOW"),
                wardRisk("Ward 6 — Kamativi", 0.38, "LOW"));
    }

    private WardRiskScore wardRisk(final String ward, final double score, final String level) {
        return WardRiskScore.builder().ward(ward).riskScore(score).riskLevel(level).build();
    }
}

