package co.edu.udea.agrocore.backend.application.service;

import co.edu.udea.agrocore.backend.application.exception.InvalidCredentialsException;
import co.edu.udea.agrocore.backend.domain.model.Role;
import co.edu.udea.agrocore.backend.domain.model.User;
import co.edu.udea.agrocore.backend.domain.port.in.AuthResult;
import co.edu.udea.agrocore.backend.domain.port.out.UserRepositoryPort;
import co.edu.udea.agrocore.backend.infrastructure.config.AgrocoreSecurityProperties;
import co.edu.udea.agrocore.backend.infrastructure.security.GoogleIdTokenValidator;
import co.edu.udea.agrocore.backend.infrastructure.security.JwtService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Orquesta el login con Google:
 * 1. Valida el ID token con Google.
 * 2. Busca o crea el usuario en BD.
 * 3. Asigna rol ADMIN si el email está en la allow-list, OPERADOR si no.
 * 4. Emite un JWT firmado de AgroCore.
 */
@Service
public class AuthenticationService {

    private final GoogleIdTokenValidator googleValidator;
    private final UserRepositoryPort userRepository;
    private final JwtService jwtService;
    private final List<String> adminEmails;

    public AuthenticationService(GoogleIdTokenValidator googleValidator,
                                 UserRepositoryPort userRepository,
                                 JwtService jwtService,
                                 AgrocoreSecurityProperties properties) {
        this.googleValidator = googleValidator;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.adminEmails = properties.adminEmails() != null ? properties.adminEmails() : List.of();
    }

    @Transactional
    public AuthResult authenticate(String googleIdToken) {
        GoogleIdToken.Payload payload = googleValidator.validate(googleIdToken)
                .orElseThrow(() -> new InvalidCredentialsException(
                        "El Google ID token es invalido o ha expirado"));

        String email = payload.getEmail();
        String googleId = payload.getSubject();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        User user = findOrCreateUser(email, googleId, name, picture);
        String jwt = jwtService.generate(user);
        return new AuthResult(jwt, user);
    }

    private User findOrCreateUser(String email, String googleId, String name, String picture) {
        Optional<User> existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            User u = existing.get();
            // Si el usuario ya existía pero sin idGoogle (creado manualmente), lo vinculamos.
            if (u.getIdGoogle() == null) {
                u.setIdGoogle(googleId);
                u.setAvatarUrl(picture);
                return userRepository.save(u);
            }
            return u;
        }
        // Nuevo usuario — determinar rol por allow-list.
        Role role = adminEmails.stream()
                .anyMatch(e -> e.trim().equalsIgnoreCase(email))
                ? Role.ADMIN : Role.OPERADOR;

        User newUser = User.builder()
                .id(UUID.randomUUID())
                .idGoogle(googleId)
                .username(name != null ? name : email)
                .email(email)
                .avatarUrl(picture)
                .role(role)
                .build();
        return userRepository.save(newUser);
    }
}
