package co.edu.udea.agrocore.backend.infrastructure.security;

import co.edu.udea.agrocore.backend.domain.model.Role;

import java.util.UUID;

/**
 * Payload verificado de un JWT emitido por AgroCore.
 */
public record DecodedJwt(UUID userId, String email, Role role) {
}
