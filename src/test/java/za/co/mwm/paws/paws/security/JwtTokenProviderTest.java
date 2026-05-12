package za.co.mwm.paws.paws.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.auth0.jwt.exceptions.JWTVerificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private static final String TEST_SECRET =
            "test-secret-key-that-is-long-enough-for-hmac256-algorithm";
    private static final Long USER_ID = 42L;
    private static final String USERNAME = "ranger1";
    private static final String ROLE = "RANGER";

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(TEST_SECRET);
    }

    @Test
    void givenValidCredentials_whenGenerateToken_shouldReturnNonNullToken() {
        final String token = jwtTokenProvider.generateToken(USER_ID, USERNAME, ROLE);
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void givenValidToken_whenExtractUsername_shouldReturnCorrectUsername() {
        final String token = jwtTokenProvider.generateToken(USER_ID, USERNAME, ROLE);
        assertThat(jwtTokenProvider.extractUsername(token)).isEqualTo(USERNAME);
    }

    @Test
    void givenValidToken_whenExtractRole_shouldReturnCorrectRole() {
        final String token = jwtTokenProvider.generateToken(USER_ID, USERNAME, ROLE);
        assertThat(jwtTokenProvider.extractRole(token)).isEqualTo(ROLE);
    }

    @Test
    void givenInvalidToken_whenValidateToken_shouldThrowJwtVerificationException() {
        assertThatThrownBy(() -> jwtTokenProvider.validateToken("invalid.token.value"))
                .isInstanceOf(JWTVerificationException.class);
    }

    @Test
    void givenTamperedToken_whenValidateToken_shouldThrowJwtVerificationException() {
        final String token = jwtTokenProvider.generateToken(USER_ID, USERNAME, ROLE);
        final String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertThatThrownBy(() -> jwtTokenProvider.validateToken(tampered))
                .isInstanceOf(JWTVerificationException.class);
    }
}

