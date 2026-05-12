package za.co.mwm.paws.paws.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import za.co.mwm.paws.paws.dto.ModelMetricsResponse;
import za.co.mwm.paws.paws.dto.ModelMetricsResponse.WardRiskScore;
import za.co.mwm.paws.paws.dto.WhatIfRequest;
import za.co.mwm.paws.paws.dto.WhatIfResponse;
import za.co.mwm.paws.paws.security.JwtTokenProvider;
import za.co.mwm.paws.paws.service.AnalyticsService;
import za.co.mwm.paws.paws.service.FileStorageService;

@WebMvcTest(AnalyticsController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
    "paws.jwt.secret=test-secret-key-that-is-long-enough-for-hmac256-algorithm",
    "paws.uploads.dir=${java.io.tmpdir}/paws-test-uploads"
})
class AnalyticsControllerTest {

    private static final String BASE_URL = "/api/analytics";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private AnalyticsService analyticsService;
    @MockitoBean private JwtTokenProvider jwtTokenProvider;
    @MockitoBean private FileStorageService fileStorageService;

    @Test
    @WithMockUser(roles = "ANALYST")
    void givenAnalystRole_whenGetModelMetrics_shouldReturn200() throws Exception {
        when(analyticsService.getModelMetrics()).thenReturn(
                ModelMetricsResponse.builder()
                        .auc(0.87).precisionAtK(0.74)
                        .modelVersion("v2.1").lastRetrained("2026-05-03T01:00:00")
                        .wardRiskScores(List.of(
                                WardRiskScore.builder().ward("Ward 1").riskScore(0.9).riskLevel("HIGH").build()))
                        .build());

        mockMvc.perform(get(BASE_URL + "/model-metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auc").value(0.87))
                .andExpect(jsonPath("$.wardRiskScores[0].ward").value("Ward 1"));
    }

    @Test
    @WithMockUser(roles = "ANALYST")
    void givenValidRequest_whenSimulateWhatIf_shouldReturn200WithProjection() throws Exception {
        final WhatIfRequest request = new WhatIfRequest("Ward 1", 2);
        when(analyticsService.simulateWhatIf(request)).thenReturn(
                WhatIfResponse.builder()
                        .ward("Ward 1").additionalRangers(2)
                        .currentAvgResponseTimeHrs(2.8).projectedAvgResponseTimeHrs(1.8)
                        .improvementPct(35.7).recommendation("High impact — strongly recommended")
                        .build());

        mockMvc.perform(post(BASE_URL + "/whatif")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.improvementPct").value(35.7));
    }
}

