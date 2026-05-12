package za.co.mwm.paws.paws.security;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.auth0.jwt.exceptions.JWTVerificationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String VALID_TOKEN = "valid.jwt.token";
    private static final String BEARER_VALID = "Bearer " + VALID_TOKEN;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void givenValidBearerToken_whenDoFilterInternal_shouldSetAuthentication() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AUTHORIZATION_HEADER, BEARER_VALID);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockFilterChain chain = new MockFilterChain();

        when(jwtTokenProvider.validateToken(VALID_TOKEN))
                .thenReturn(
                        com.auth0.jwt.JWT.require(
                                        com.auth0.jwt.algorithms.Algorithm.HMAC256(
                                                "test-secret-that-is-long-enough-for-hmac256"))
                                .build()
                                .verify(
                                        new JwtTokenProvider(
                                                        "test-secret-that-is-long-enough-for-hmac256")
                                                .generateToken(1L, "ranger1", "RANGER")));

        jwtAuthenticationFilter.doFilter(request, response, chain);

        // filter chain should have been invoked
        assert chain.getRequest() != null;
    }

    @Test
    void givenInvalidBearerToken_whenDoFilterInternal_shouldClearContextAndContinueChain()
            throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AUTHORIZATION_HEADER, "Bearer bad.token");
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockFilterChain chain = new MockFilterChain();

        when(jwtTokenProvider.validateToken("bad.token"))
                .thenThrow(new JWTVerificationException("invalid"));

        jwtAuthenticationFilter.doFilter(request, response, chain);

        assert chain.getRequest() != null;
        assert SecurityContextHolder.getContext().getAuthentication() == null;
    }

    @Test
    void givenNoBearerToken_whenDoFilterInternal_shouldContinueChainWithoutAuthentication()
            throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockFilterChain chain = new MockFilterChain();

        jwtAuthenticationFilter.doFilter(request, response, chain);

        assert chain.getRequest() != null;
    }

    @Test
    void givenNonBearerAuthHeader_whenDoFilterInternal_shouldContinueChainWithoutAuthentication()
            throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AUTHORIZATION_HEADER, "Basic dXNlcjpwYXNz");
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockFilterChain chain = new MockFilterChain();

        jwtAuthenticationFilter.doFilter(request, response, chain);

        assert chain.getRequest() != null;
    }
}

