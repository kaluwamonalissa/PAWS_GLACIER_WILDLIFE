package za.co.mwm.paws.paws.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
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
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private FileStorageService fileStorageService;

    @MockitoBean
    private WildlifeService wildlifeService;

    @Test
    void givenIllegalArgumentException_whenHandled_shouldReturn400WithErrorBody() throws Exception {
        when(wildlifeService.getEndangeredCategories())
                .thenThrow(new IllegalArgumentException("Bad input"));

        mockMvc.perform(get("/api/wildlife/categories"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad input"));
    }
}
