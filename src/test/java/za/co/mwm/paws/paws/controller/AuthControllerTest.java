package za.co.mwm.paws.paws.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import za.co.mwm.paws.paws.dto.LoginRequest;
import za.co.mwm.paws.paws.dto.LoginResponse;
import za.co.mwm.paws.paws.dto.RegisterRequest;
import za.co.mwm.paws.paws.security.JwtTokenProvider;
import za.co.mwm.paws.paws.service.AuthService;
import za.co.mwm.paws.paws.service.FileStorageService;

@WebMvcTest(AuthController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@org.springframework.test.context.TestPropertySource(
        properties = {
            "paws.jwt.secret=test-secret-key-that-is-long-enough-for-hmac256-algorithm",
            "paws.uploads.dir=${java.io.tmpdir}/paws-test-uploads"
        })
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private FileStorageService fileStorageService;

    @Test
    void givenValidCredentials_whenLogin_shouldReturn200WithToken() throws Exception {
        final LoginRequest request = new LoginRequest("ranger1", "password");
        final LoginResponse response =
                LoginResponse.builder()
                        .token("test-token")
                        .role("RANGER")
                        .username("ranger1")
                        .fullName("Ranger Blessing Dube")
                        .userId(4L)
                        .build();

        when(authService.login(request)).thenReturn(response);

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-token"))
                .andExpect(jsonPath("$.role").value("RANGER"));
    }

    @Test
    void givenValidRegistration_whenRegister_shouldReturn200WithCommunityRole() throws Exception {
        final RegisterRequest request = new RegisterRequest("newuser", "pass", "New User");
        final LoginResponse response =
                LoginResponse.builder()
                        .token("token")
                        .role("COMMUNITY")
                        .username("newuser")
                        .fullName("New User")
                        .userId(10L)
                        .build();

        when(authService.register(request)).thenReturn(response);

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("COMMUNITY"));
    }

    @Test
    void givenInvalidCredentials_whenLogin_shouldReturn400() throws Exception {
        final LoginRequest request = new LoginRequest("bad", "bad");
        when(authService.login(request))
                .thenThrow(new IllegalArgumentException("Invalid username or password"));

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}








