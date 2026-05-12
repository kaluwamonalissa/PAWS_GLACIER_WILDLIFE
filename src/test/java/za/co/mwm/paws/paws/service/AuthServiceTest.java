package za.co.mwm.paws.paws.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import za.co.mwm.paws.paws.domain.Role;
import za.co.mwm.paws.paws.domain.User;
import za.co.mwm.paws.paws.dto.LoginRequest;
import za.co.mwm.paws.paws.dto.LoginResponse;
import za.co.mwm.paws.paws.dto.RegisterRequest;
import za.co.mwm.paws.paws.repository.UserRepository;
import za.co.mwm.paws.paws.security.JwtTokenProvider;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String TEST_SECRET =
            "test-secret-key-that-is-long-enough-for-hmac256-algorithm";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private JwtTokenProvider jwtTokenProvider;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(TEST_SECRET);
        authService = new AuthService(userRepository, passwordEncoder, jwtTokenProvider);
    }

    @Test
    void givenValidCredentials_whenLogin_shouldReturnTokenAndRole() {
        final User user = buildUser(1L, "ranger1", "hashed", Role.RANGER);
        when(userRepository.findByUsername("ranger1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashed")).thenReturn(true);

        final LoginResponse response =
                authService.login(new LoginRequest("ranger1", "password"));

        assertThat(response.getToken()).isNotNull();
        assertThat(response.getRole()).isEqualTo("RANGER");
        assertThat(response.getUsername()).isEqualTo("ranger1");
    }

    @Test
    void givenUnknownUsername_whenLogin_shouldThrowIllegalArgumentException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("unknown", "pass")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid");
    }

    @Test
    void givenWrongPassword_whenLogin_shouldThrowIllegalArgumentException() {
        final User user = buildUser(1L, "ranger1", "hashed", Role.RANGER);
        when(userRepository.findByUsername("ranger1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("ranger1", "wrong")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void givenNewUsername_whenRegister_shouldCreateCommunityUser() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(inv -> {
            final User u = inv.getArgument(0);
            u.setId(99L);
            return u;
        });

        final LoginResponse response =
                authService.register(new RegisterRequest("newuser", "pass", "New User"));

        assertThat(response.getRole()).isEqualTo("RANCHER");
        assertThat(response.getUsername()).isEqualTo("newuser");
    }

    @Test
    void givenExistingUsername_whenRegister_shouldThrowIllegalArgumentException() {
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThatThrownBy(
                        () ->
                                authService.register(
                                        new RegisterRequest("existing", "pass", "Name")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("taken");
    }

    private User buildUser(
            final Long id, final String username, final String hash, final Role role) {
        return User.builder()
                .id(id)
                .username(username)
                .passwordHash(hash)
                .fullName("Test User")
                .role(role)
                .build();
    }
}

