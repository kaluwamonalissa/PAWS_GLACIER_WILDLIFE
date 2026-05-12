package za.co.mwm.paws.paws.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_USER_ID = "userId";
    private static final long TOKEN_VALIDITY_HOURS = 24L;

    private final Algorithm algorithm;

    public JwtTokenProvider(@Value("${paws.jwt.secret}") final String secret) {
        this.algorithm = Algorithm.HMAC256(secret);
    }

    public String generateToken(final Long userId, final String username, final String role) {
        return JWT.create()
                .withSubject(username)
                .withClaim(CLAIM_USER_ID, userId)
                .withClaim(CLAIM_ROLE, role)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plus(TOKEN_VALIDITY_HOURS, ChronoUnit.HOURS))
                .sign(algorithm);
    }

    public DecodedJWT validateToken(final String token) throws JWTVerificationException {
        return JWT.require(algorithm).build().verify(token);
    }

    public String extractUsername(final String token) {
        return validateToken(token).getSubject();
    }

    public String extractRole(final String token) {
        return validateToken(token).getClaim(CLAIM_ROLE).asString();
    }
}

