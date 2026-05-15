package co.edu.udea.agrocore.backend.infrastructure.security;

import co.edu.udea.agrocore.backend.infrastructure.config.AgrocoreSecurityProperties;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;

/**
 * Valida Google ID tokens usando la librería oficial de Google.
 * Verifica firma, audiencia y expiración. Solo acepta tokens emitidos para
 * el {@code AGROCORE_GOOGLE_CLIENT_ID} configurado.
 */
@Component
public class GoogleIdTokenValidator {

    private static final Logger log = LoggerFactory.getLogger(GoogleIdTokenValidator.class);

    private final GoogleIdTokenVerifier verifier;

    public GoogleIdTokenValidator(AgrocoreSecurityProperties properties) {
        String clientId = properties.google().clientId();
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    /**
     * Valida el token y devuelve el payload si es válido.
     *
     * @param idToken string del ID token de Google
     * @return Optional con el payload, o vacío si el token es inválido
     */
    public Optional<GoogleIdToken.Payload> validate(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            return Optional.empty();
        }
        try {
            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                log.debug("Google ID token invalido o expirado");
                return Optional.empty();
            }
            return Optional.of(token.getPayload());
        } catch (Exception e) {
            log.debug("Error verificando Google ID token: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
