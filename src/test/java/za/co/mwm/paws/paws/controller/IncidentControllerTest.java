package za.co.mwm.paws.paws.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
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
import za.co.mwm.paws.paws.domain.IncidentStatus;
import za.co.mwm.paws.paws.domain.IncidentType;
import za.co.mwm.paws.paws.dto.HeatmapPoint;
import za.co.mwm.paws.paws.dto.IncidentRequest;
import za.co.mwm.paws.paws.dto.IncidentResponse;
import za.co.mwm.paws.paws.dto.IncidentUpdateRequest;
import za.co.mwm.paws.paws.security.JwtTokenProvider;
import za.co.mwm.paws.paws.service.FileStorageService;
import za.co.mwm.paws.paws.service.IncidentService;

@WebMvcTest(IncidentController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(
        properties = {
            "paws.jwt.secret=test-secret-key-that-is-long-enough-for-hmac256-algorithm",
            "paws.uploads.dir=${java.io.tmpdir}/paws-test-uploads"
        })
class IncidentControllerTest {

    private static final String BASE_URL = "/api/incidents";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IncidentService incidentService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private FileStorageService fileStorageService;

    @Test
    @WithMockUser(username = "ranger1", roles = "RANGER")
    void givenValidRequest_whenCreateIncident_shouldReturn200() throws Exception {
        final IncidentRequest request = IncidentRequest.builder()
                .type(IncidentType.POACHING).description("Snare found")
                .latitude(-18.93).longitude(26.48).build();
        final IncidentResponse response = buildIncidentResponse(1L, IncidentType.POACHING, IncidentStatus.RECEIVED);

        when(incidentService.createIncident(any(), anyString())).thenReturn(response);

        mockMvc.perform(
                        post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .principal(() -> "ranger1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("POACHING"));
    }

    @Test
    @WithMockUser(roles = "RANGER")
    void givenExistingIncidents_whenGetAllIncidents_shouldReturn200WithList() throws Exception {
        when(incidentService.getAllIncidents(null, null, null)).thenReturn(List.of(
                buildIncidentResponse(1L, IncidentType.VELD_FIRE, IncidentStatus.RECEIVED)));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("VELD_FIRE"));
    }

    @Test
    @WithMockUser(username = "ranger1", roles = "RANGER")
    void givenValidIncidentId_whenRespondToIncident_shouldReturn200() throws Exception {
        final IncidentResponse response =
                buildIncidentResponse(2L, IncidentType.POACHING, IncidentStatus.RANGER_ASSIGNED);
        when(incidentService.respondToIncident(any(), anyString())).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/2/respond").principal(() -> "ranger1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RANGER_ASSIGNED"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void givenIncidents_whenGetHeatmap_shouldReturn200WithPoints() throws Exception {
        when(incidentService.getHeatmap()).thenReturn(
                List.of(HeatmapPoint.builder()
                        .latitude(-18.93)
                        .longitude(26.48)
                        .type(IncidentType.POACHING)
                        .count(1L)
                        .build()));

        mockMvc.perform(get(BASE_URL + "/heatmap"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("POACHING"));
    }

    @Test
    @WithMockUser(username = "ranger1", roles = "RANGER")
    void givenValidUpdateRequest_whenUpdateIncident_shouldReturn200() throws Exception {
        final IncidentUpdateRequest updateRequest = IncidentUpdateRequest.builder()
                .actionTaken("Used chilli fence").deterrentsUsed("Chilli").outcome("Resolved").build();
        final IncidentResponse response =
                buildIncidentResponse(1L, IncidentType.POACHING, IncidentStatus.RESOLVED);
        when(incidentService.updateIncident(any(), any(), anyString())).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .principal(() -> "ranger1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));
    }

    @Test
    @WithMockUser(username = "rancher1", roles = "RANCHER")
    void givenRancherUser_whenGetMyIncidents_shouldReturn200() throws Exception {
        when(incidentService.getMyIncidents(anyString())).thenReturn(List.of());
        mockMvc.perform(get(BASE_URL + "/mine").principal(() -> "rancher1"))
                .andExpect(status().isOk());
    }

    private IncidentResponse buildIncidentResponse(
            final Long id, final IncidentType type, final IncidentStatus status) {
        return IncidentResponse.builder().id(id).type(type).status(status)
                .description("Test").latitude(-18.93).longitude(26.48)
                .reportedBy("Test User").timestamp(LocalDateTime.now()).responders(List.of()).build();
    }
}

