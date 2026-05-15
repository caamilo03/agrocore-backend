package co.edu.udea.agrocore.backend.infrastructure.adapter.in.web.dto;

/** Respuesta del endpoint POST /api/v1/auth/google. */
public record LoginResponse(String token, UserDto user) {
}
