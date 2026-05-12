package za.co.mwm.paws.paws.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import za.co.mwm.paws.paws.dto.WildlifeCategoryResponse;
import za.co.mwm.paws.paws.security.JwtTokenProvider;
import za.co.mwm.paws.paws.service.FileStorageService;
import za.co.mwm.paws.paws.service.WildlifeService;

@WebMvcTest(WildlifeController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(
        properties = {
            "paws.jwt.secret=test-secret-key-that-is-long-enough-for-hmac256-algorithm",
            "paws.uploads.dir=${java.io.tmpdir}/paws-test-uploads"
        })
class WildlifeControllerTest {

    private static final String CATEGORIES_URL = "/api/wildlife/categories";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WildlifeService wildlifeService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private FileStorageService fileStorageService;

    @Test
    @WithMockUser(roles = "RANGER")
    void givenCategories_whenGetCategories_shouldReturn200WithList() throws Exception {
        when(wildlifeService.getEndangeredCategories()).thenReturn(
                List.of(WildlifeCategoryResponse.builder()
                        .category("Mega-Herbivores")
                        .species(List.of("African Elephant"))
                        .riskLevel("HIGH")
                        .build()));

        mockMvc.perform(get(CATEGORIES_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("Mega-Herbivores"))
                .andExpect(jsonPath("$[0].riskLevel").value("HIGH"));
    }
}

