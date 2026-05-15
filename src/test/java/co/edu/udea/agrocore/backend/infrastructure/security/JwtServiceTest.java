package co.edu.udea.agrocore.backend.infrastructure.security;

import co.edu.udea.agrocore.backend.domain.model.Role;
import co.edu.udea.agrocore.backend.domain.model.User;
import co.edu.udea.agrocore.backend.infrastructure.config.AgrocoreSecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET =
            "test-secret-at-least-32-chars-long-for-hs256-ok!";
    private static final long EXPIRY_MS = 3_600_000L; // 1h

    private JwtService jwtService;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        var props = new AgrocoreSecurityProperties(
                new AgrocoreSecurityProperties.Jwt(SECRET, EXPIRY_MS),
                List.of("admin@test.com"),
                new AgrocoreSecurityProperties.Google("google-client-id")
        );
        jwtService = new JwtService(props);
        sampleUser = User.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .email("operador@test.com")
                .role(Role.OPERADOR)
                .username("Op Test")
                .build();
    }

    @Test
    void generateAndParse_roundTripPreservesAllClaims() {
        String token = jwtService.generate(sampleUser);
        Optional<DecodedJwt> result = jwtService.parse(token);

        assertThat(result).isPresent();
        assertThat(result.get().userId()).isEqualTo(sampleUser.getId());
        assertThat(result.get().email()).isEqualTo(sampleUser.getEmail());
        assertThat(result.get().role()).isEqualTo(Role.OPERADOR);
    }

    @Test
    void parse_returnsEmptyForGarbageToken() {
        assertThat(jwtService.parse("not.a.jwt")).isEmpty();
        assertThat(jwtService.parse("")).isEmpty();
        assertThat(jwtService.parse(null)).isEmpty();
    }

    @Test
    void parse_returnsEmptyForTokenSignedWithDifferentSecret() {
        var otherProps = new AgrocoreSecurityProperties(
                new AgrocoreSecurityProperties.Jwt("other-secret-also-32-chars-long-ok!", EXPIRY_MS),
                List.of(), new AgrocoreSecurityProperties.Google("")
        );
        var otherService = new JwtService(otherProps);
        String token = otherService.generate(sampleUser);

        assertThat(jwtService.parse(token)).isEmpty();
    }

    @Test
    void parse_returnsEmptyForExpiredToken() throws InterruptedException {
        var shortProps = new AgrocoreSecurityProperties(
                new AgrocoreSecurityProperties.Jwt(SECRET, 1L), // expira en 1ms
                List.of(), new AgrocoreSecurityProperties.Google("")
        );
        var shortService = new JwtService(shortProps);
        String token = shortService.generate(sampleUser);
        Thread.sleep(10);

        assertThat(shortService.parse(token)).isEmpty();
    }

    @Test
    void generate_worksForAllRoles() {
        for (Role role : Role.values()) {
            User u = User.builder().id(UUID.randomUUID()).email("x@x.com").role(role).username("X").build();
            String token = jwtService.generate(u);
            assertThat(jwtService.parse(token)).isPresent()
                    .hasValueSatisfying(d -> assertThat(d.role()).isEqualTo(role));
        }
    }
}
