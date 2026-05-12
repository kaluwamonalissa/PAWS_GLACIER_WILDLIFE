package za.co.mwm.paws.paws.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import za.co.mwm.paws.paws.dto.SightingResponse;
import za.co.mwm.paws.paws.security.JwtTokenProvider;
import za.co.mwm.paws.paws.service.FileStorageService;
import za.co.mwm.paws.paws.service.SightingService;

@WebMvcTest(SightingController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(
        properties = {
            "paws.jwt.secret=test-secret-key-that-is-long-enough-for-hmac256-algorithm",
            "paws.uploads.dir=${java.io.tmpdir}/paws-test-uploads"
        })
class SightingControllerTest {

    private static final String SIGHTINGS_URL = "/api/sightings";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SightingService sightingService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private FileStorageService fileStorageService;

    @Test
    @WithMockUser(username = "ranger1", roles = "RANGER")
    void givenValidSightingForm_whenCreateSighting_shouldReturn200() throws Exception {
        final SightingResponse response =
                SightingResponse.builder()
                        .id(1L)
                        .species("African Elephant")
                        .description("Large herd")
                        .latitude(-18.93)
                        .longitude(26.48)
                        .reportedBy("Ranger Blessing Dube")
                        .timestamp(LocalDateTime.now())
                        .build();

        when(sightingService.createSighting(anyString(), anyString(), anyDouble(), anyDouble(), any(), anyString()))
                .thenReturn(response);

        final MockMultipartFile photo =
                new MockMultipartFile("photo", "img.jpg", "image/jpeg", "data".getBytes());

        mockMvc.perform(
                        multipart(SIGHTINGS_URL)
                                .file(photo)
                                .param("species", "African Elephant")
                                .param("description", "Large herd")
                                .param("latitude", "-18.93")
                                .param("longitude", "26.48")
                                .principal(() -> "ranger1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.species").value("African Elephant"));
    }
}

