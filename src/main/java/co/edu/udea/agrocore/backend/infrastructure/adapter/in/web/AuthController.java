package co.edu.udea.agrocore.backend.infrastructure.adapter.in.web;

import co.edu.udea.agrocore.backend.application.service.AuthenticationService;
import co.edu.udea.agrocore.backend.domain.port.in.AuthResult;
import co.edu.udea.agrocore.backend.infrastructure.adapter.in.web.dto.LoginRequest;
import co.edu.udea.agrocore.backend.infrastructure.adapter.in.web.dto.LoginResponse;
import co.edu.udea.agrocore.backend.infrastructure.adapter.in.web.dto.UserDto;
import co.edu.udea.agrocore.backend.infrastructure.security.AuthenticatedUser;
import co.edu.udea.agrocore.backend.infrastructure.security.DecodedJwt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

/**
 * Endpoints de autenticacion. Son publicos (no requieren token).
 *
 * POST /api/v1/auth/google — intercambia un Google ID token por un JWT de AgroCore.
 * GET  /api/v1/auth/me    — devuelve el usuario actual (requiere token valido).
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final AuthenticatedUser authenticatedUser;
    private final co.edu.udea.agrocore.backend.domain.port.out.UserRepositoryPort userRepository;

    public AuthController(AuthenticationService authenticationService,
                          AuthenticatedUser authenticatedUser,
                          co.edu.udea.agrocore.backend.domain.port.out.UserRepositoryPort userRepository) {
        this.authenticationService = authenticationService;
        this.authenticatedUser = authenticatedUser;
        this.userRepository = userRepository;
    }

    /**
     * Recibe el Google ID token del frontend, lo valida, y emite un JWT de AgroCore.
     *
     * @return 200 con { token, user } si el token es valido.
     *         400 si no se proporciona idToken.
     *         401 si el token de Google es invalido o expirado.
     */
    @PostMapping("/google")
    public ResponseEntity<LoginResponse> loginWithGoogle(@RequestBody LoginRequest request) {
        if (request.idToken() == null || request.idToken().isBlank()) {
            throw new IllegalArgumentException("El campo idToken es obligatorio");
        }
        AuthResult result = authenticationService.authenticate(request.idToken());
        return ResponseEntity.ok(new LoginResponse(result.jwt(), UserDto.from(result.user())));
    }

    /**
     * Devuelve el perfil del usuario autenticado actual.
     *
     * @return 200 con el UserDto si el Bearer token es valido.
     *         401 si no hay token o es invalido.
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> me() {
        DecodedJwt decoded = authenticatedUser.getCurrent()
                .orElseThrow(() -> new co.edu.udea.agrocore.backend.application.exception
                        .InvalidCredentialsException("Token de autenticacion requerido"));
        UserDto userDto = userRepository.findById(decoded.userId())
                .map(UserDto::from)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
        return ResponseEntity.ok(userDto);
    }
}
