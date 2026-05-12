package za.co.mwm.paws.paws.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import za.co.mwm.paws.paws.domain.IncidentType;
import za.co.mwm.paws.paws.dto.DashboardSummaryResponse;
import za.co.mwm.paws.paws.dto.HeatmapPoint;
import za.co.mwm.paws.paws.security.JwtTokenProvider;
import za.co.mwm.paws.paws.service.DashboardService;
import za.co.mwm.paws.paws.service.FileStorageService;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
    "paws.jwt.secret=test-secret-key-that-is-long-enough-for-hmac256-algorithm",
    "paws.uploads.dir=${java.io.tmpdir}/paws-test-uploads"
})
class DashboardControllerTest {

    private static final String BASE_URL = "/api/dashboard";

    @Autowired private MockMvc mockMvc;
    @MockitoBean private DashboardService dashboardService;
    @MockitoBean private JwtTokenProvider jwtTokenProvider;
    @MockitoBean private FileStorageService fileStorageService;

    @Test
    @WithMockUser(roles = "MANAGER")
    void givenManagerRole_whenGetSummary_shouldReturn200WithStats() throws Exception {
        when(dashboardService.getSummary()).thenReturn(
                DashboardSummaryResponse.builder()
                        .incidentsThisWeek(5).resolvedThisWeek(2)
                        .openIncidents(3).avgResponseTimeHrs(2.3)
                        .hotspots(List.of(HeatmapPoint.builder()
                                .latitude(-18.93).longitude(26.48)
                                .type(IncidentType.POACHING).count(1L).build()))
                        .build());

        mockMvc.perform(get(BASE_URL + "/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incidentsThisWeek").value(5))
                .andExpect(jsonPath("$.avgResponseTimeHrs").value(2.3));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void givenManagerRole_whenExportCsv_shouldReturnCsvContent() throws Exception {
        when(dashboardService.exportCsv(null, null, null))
                .thenReturn("id,type,status\n1,POACHING,RECEIVED\n");

        mockMvc.perform(get(BASE_URL + "/export"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("POACHING")));
    }
}

