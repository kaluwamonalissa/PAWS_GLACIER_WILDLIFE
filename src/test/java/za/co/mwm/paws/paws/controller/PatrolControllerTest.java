package za.co.mwm.paws.paws.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
import za.co.mwm.paws.paws.dto.PatrolRequest;
import za.co.mwm.paws.paws.dto.PatrolResponse;
import za.co.mwm.paws.paws.security.JwtTokenProvider;
import za.co.mwm.paws.paws.service.FileStorageService;
import za.co.mwm.paws.paws.service.PatrolService;

@WebMvcTest(PatrolController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
    "paws.jwt.secret=test-secret-key-that-is-long-enough-for-hmac256-algorithm",
    "paws.uploads.dir=${java.io.tmpdir}/paws-test-uploads"
})
class PatrolControllerTest {

    private static final String BASE_URL = "/api/patrols";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private PatrolService patrolService;
    @MockitoBean private JwtTokenProvider jwtTokenProvider;
    @MockitoBean private FileStorageService fileStorageService;

    @Test
    @WithMockUser(username = "ranger1", roles = "RANGER")
    void givenValidPatrolData_whenLogPatrol_shouldReturn200() throws Exception {
        final PatrolRequest request = PatrolRequest.builder()
                .startLatitude(-18.92).startLongitude(26.46)
                .distanceKm(4.2).snarersRemoved(3).notes("Test patrol").ward("Ward 1").build();

        when(patrolService.logPatrol(any(), anyString())).thenReturn(
                PatrolResponse.builder().id(1L).rangerName("Ranger Blessing Dube")
                        .startLatitude(-18.92).startLongitude(26.46)
                        .distanceKm(4.2).snarersRemoved(3).ward("Ward 1")
                        .startedAt(LocalDateTime.now()).build());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(() -> "ranger1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rangerName").value("Ranger Blessing Dube"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void givenManagerRole_whenGetAllPatrols_shouldReturn200WithList() throws Exception {
        when(patrolService.getAllPatrols()).thenReturn(List.of(
                PatrolResponse.builder().id(1L).rangerName("Ranger 1")
                        .startLatitude(-18.92).startLongitude(26.46)
                        .startedAt(LocalDateTime.now()).build()));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rangerName").value("Ranger 1"));
    }

    @Test
    @WithMockUser(username = "ranger1", roles = "RANGER")
    void givenRangerRole_whenGetMyPatrols_shouldReturn200() throws Exception {
        when(patrolService.getMyPatrols(anyString())).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL + "/mine").principal(() -> "ranger1"))
                .andExpect(status().isOk());
    }
}

