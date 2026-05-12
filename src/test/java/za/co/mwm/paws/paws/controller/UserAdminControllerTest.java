package za.co.mwm.paws.paws.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import za.co.mwm.paws.paws.domain.Role;
import za.co.mwm.paws.paws.dto.UserResponse;
import za.co.mwm.paws.paws.security.JwtTokenProvider;
import za.co.mwm.paws.paws.service.FileStorageService;
import za.co.mwm.paws.paws.service.UserAdminService;

@WebMvcTest(UserAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(
        properties = {
            "paws.jwt.secret=test-secret-key-that-is-long-enough-for-hmac256-algorithm",
            "paws.uploads.dir=${java.io.tmpdir}/paws-test-uploads"
        })
class UserAdminControllerTest {

    private static final String ADMIN_USERS_URL = "/api/admin/users";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserAdminService userAdminService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private FileStorageService fileStorageService;

    @Test
    @WithMockUser(roles = "SENIOR_MANAGER")
    void givenUsers_whenGetAllUsers_shouldReturn200WithList() throws Exception {
        when(userAdminService.getAllUsers()).thenReturn(
                List.of(UserResponse.builder()
                        .id(1L)
                        .username("community1")
                        .fullName("Themba Mpofu")
                        .role(Role.RANCHER)
                        .active(true)
                        .build()));

        mockMvc.perform(get(ADMIN_USERS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("community1"));
    }

    @Test
    @WithMockUser(roles = "SENIOR_MANAGER")
    void givenValidRoleChange_whenUpdateUserRole_shouldReturn200WithUpdatedUser() throws Exception {
        final UserResponse updated =
                UserResponse.builder().id(5L).username("community1").fullName("Themba").role(Role.RANGER).build();
        when(userAdminService.updateUserRole(any(), any())).thenReturn(updated);

        mockMvc.perform(patch(ADMIN_USERS_URL + "/5/role").param("role", "RANGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("RANGER"));
    }
}

