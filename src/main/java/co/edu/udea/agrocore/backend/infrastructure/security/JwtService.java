package co.edu.udea.agrocore.backend.infrastructure.security;

import co.edu.udea.agrocore.backend.domain.model.Role;
import co.edu.udea.agrocore.backend.domain.model.User;
import co.edu.udea.agrocore.backend.infrastructure.config.AgrocoreSecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Genera y verifica JWT firmados con HS256.
 *
 * Claims del token:
 * <ul>
 *   <li>{@code sub} — userId (UUID como string)</li>
 *   <li>{@code email} — email del usuario</li>
 *   <li>{@code role} — nombre del rol (ADMIN / OPERADOR / OBSERVADOR)</li>
 * </ul>
 */
@Service
public class JwtService {

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLE = "role";

    private final SecretKey secretKey;
    private final long expiryMs;

    public JwtService(AgrocoreSecurityProperties properties) {
        this.secretKey = Keys.hmacShaKeyFor(
                properties.jwt().secret().getBytes(StandardCharsets.UTF_8));
        this.expiryMs = properties.jwt().expiryMs();
    }

    /** Genera un JWT firmado para el usuario dado. */
    public String generate(User user) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim(CLAIM_EMAIL, user.getEmail())
                .claim(CLAIM_ROLE, user.getRole().name())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiryMs))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Parsea y verifica la firma y la expiración del token.
     *
     * @return Optional vacío si el token es inválido o ha expirado.
     */
    public Optional<DecodedJwt> parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            UUID userId = UUID.fromString(claims.getSubject());
            String email = claims.get(CLAIM_EMAIL, String.class);
            Role role = Role.fromString(claims.get(CLAIM_ROLE, String.class));
            return Optional.of(new DecodedJwt(userId, email, role));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
