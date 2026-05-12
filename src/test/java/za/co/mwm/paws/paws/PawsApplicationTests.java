package za.co.mwm.paws.paws;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(
        properties = {
            "paws.jwt.secret=test-secret-key-that-is-long-enough-for-hmac256-algorithm",
            "paws.uploads.dir=${java.io.tmpdir}/paws-test-uploads"
        })
class PawsApplicationTests {

    @Test
    void givenSpringContext_whenApplicationStarts_shouldLoadWithoutErrors() {
        assertThatCode(() -> {}).doesNotThrowAnyException();
    }
}
