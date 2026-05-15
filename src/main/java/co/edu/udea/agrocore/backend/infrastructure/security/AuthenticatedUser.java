package co.edu.udea.agrocore.backend.infrastructure.security;

import co.edu.udea.agrocore.backend.domain.model.Role;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Optional;
import java.util.UUID;

/**
 * Helper inyectable (request-scoped) que expone el usuario autenticado actual
 * extraído del SecurityContext. Los controllers y services lo inyectan para
 * saber quién está haciendo la operación y qué rol tiene.
 *
 * Si no hay token válido en el request, los métodos devuelven Optional.empty().
 */
@Component
@RequestScope
public class AuthenticatedUser {

    public Optional<DecodedJwt> getCurrent() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof DecodedJwt decoded) {
            return Optional.of(decoded);
        }
        return Optional.empty();
    }

    public Optional<UUID> getUserId() {
        return getCurrent().map(DecodedJwt::userId);
    }

    public Optional<Role> getRole() {
        return getCurrent().map(DecodedJwt::role);
    }

    public boolean isAdmin() {
        return getRole().map(r -> r == Role.ADMIN).orElse(false);
    }

    public boolean isOperador() {
        return getRole().map(r -> r == Role.OPERADOR).orElse(false);
    }

    public boolean isObservador() {
        return getRole().map(r -> r == Role.OBSERVADOR).orElse(false);
    }
}
