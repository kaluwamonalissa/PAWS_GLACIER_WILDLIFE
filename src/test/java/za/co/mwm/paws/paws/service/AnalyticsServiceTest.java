package za.co.mwm.paws.paws.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.mwm.paws.paws.dto.ModelMetricsResponse;
import za.co.mwm.paws.paws.dto.WhatIfRequest;
import za.co.mwm.paws.paws.dto.WhatIfResponse;

class AnalyticsServiceTest {

    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsService();
    }

    @Test
    void givenNoInput_whenGetModelMetrics_shouldReturnMetricsWithSixWards() {
        final ModelMetricsResponse metrics = analyticsService.getModelMetrics();
        assertThat(metrics.getAuc()).isGreaterThan(0);
        assertThat(metrics.getPrecisionAtK()).isGreaterThan(0);
        assertThat(metrics.getWardRiskScores()).hasSize(6);
    }

    @Test
    void givenNoInput_whenGetModelMetrics_shouldIncludeHighRiskWard() {
        final ModelMetricsResponse metrics = analyticsService.getModelMetrics();
        assertThat(metrics.getWardRiskScores())
                .anyMatch(w -> "HIGH".equals(w.getRiskLevel()));
    }

    @Test
    void givenNoInput_whenGetModelMetrics_shouldHaveNonNullModelVersion() {
        assertThat(analyticsService.getModelMetrics().getModelVersion()).isNotBlank();
    }

    @Test
    void givenWhatIfRequest_whenSimulate_shouldReturnProjectedResponseTime() {
        final WhatIfRequest request = new WhatIfRequest("Ward 1", 2);
        final WhatIfResponse response = analyticsService.simulateWhatIf(request);
        assertThat(response.getProjectedAvgResponseTimeHrs())
                .isLessThan(response.getCurrentAvgResponseTimeHrs());
        assertThat(response.getImprovementPct()).isGreaterThan(0);
    }

    @Test
    void givenManyExtraRangers_whenSimulate_shouldNotProjectNegativeResponseTime() {
        final WhatIfRequest request = new WhatIfRequest("Ward 2", 100);
        final WhatIfResponse response = analyticsService.simulateWhatIf(request);
        assertThat(response.getProjectedAvgResponseTimeHrs()).isGreaterThanOrEqualTo(0.5);
    }

    @Test
    void givenOneExtraRanger_whenSimulate_shouldProvideRecommendation() {
        final WhatIfRequest request = new WhatIfRequest("Ward 3", 1);
        final WhatIfResponse response = analyticsService.simulateWhatIf(request);
        assertThat(response.getRecommendation()).isNotBlank();
    }
}

