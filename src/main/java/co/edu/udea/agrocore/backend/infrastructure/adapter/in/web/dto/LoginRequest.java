package co.edu.udea.agrocore.backend.infrastructure.adapter.in.web.dto;

/** Body del endpoint POST /api/v1/auth/google. */
public record LoginRequest(String idToken) {
}
