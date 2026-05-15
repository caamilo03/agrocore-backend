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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {

    private GoogleIdTokenValidator googleValidator;
    private UserRepositoryPort userRepository;
    private JwtService jwtService;
    private AuthenticationService service;

    @BeforeEach
    void setUp() {
        googleValidator = mock(GoogleIdTokenValidator.class);
        userRepository = mock(UserRepositoryPort.class);
        jwtService = mock(JwtService.class);
        var props = new AgrocoreSecurityProperties(
                new AgrocoreSecurityProperties.Jwt("secret", 86400000L),
                List.of("admin@agrocore.com"),
                new AgrocoreSecurityProperties.Google("client-id")
        );
        service = new AuthenticationService(googleValidator, userRepository, jwtService, props);
    }

    @Test
    void authenticate_newUser_notInAdminList_createsAsOperador() {
        GoogleIdToken.Payload payload = mockPayload("user@example.com", "sub123", "User Name", "http://pic");
        when(googleValidator.validate("id-token")).thenReturn(Optional.of(payload));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generate(any())).thenReturn("jwt-token");

        AuthResult result = service.authenticate("id-token");

        assertThat(result.user().getRole()).isEqualTo(Role.OPERADOR);
        assertThat(result.user().getEmail()).isEqualTo("user@example.com");
        assertThat(result.jwt()).isEqualTo("jwt-token");
    }

    @Test
    void authenticate_newUser_inAdminList_createsAsAdmin() {
        GoogleIdToken.Payload payload = mockPayload("admin@agrocore.com", "sub456", "Admin", "http://pic");
        when(googleValidator.validate("id-token")).thenReturn(Optional.of(payload));
        when(userRepository.findByEmail("admin@agrocore.com")).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generate(any())).thenReturn("jwt-token");

        AuthResult result = service.authenticate("id-token");

        assertThat(result.user().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void authenticate_existingUser_returnsExistingWithoutChangingRole() {
        GoogleIdToken.Payload payload = mockPayload("existing@example.com", "sub789", "Existing", "http://pic");
        when(googleValidator.validate("id-token")).thenReturn(Optional.of(payload));
        User existing = User.builder().id(UUID.randomUUID()).email("existing@example.com")
                .idGoogle("sub789").role(Role.OBSERVADOR).username("Existing").build();
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existing));
        when(jwtService.generate(any())).thenReturn("jwt");

        AuthResult result = service.authenticate("id-token");

        assertThat(result.user().getRole()).isEqualTo(Role.OBSERVADOR);
        verify(userRepository, never()).save(any()); // no modifica usuario existente
    }

    @Test
    void authenticate_existingUserWithoutGoogleId_linksGoogleAccount() {
        GoogleIdToken.Payload payload = mockPayload("seed@example.com", "new-sub", "Seed", "http://pic");
        when(googleValidator.validate("id-token")).thenReturn(Optional.of(payload));
        User seedUser = User.builder().id(UUID.randomUUID()).email("seed@example.com")
                .idGoogle(null).role(Role.ADMIN).username("Seed").build();
        when(userRepository.findByEmail("seed@example.com")).thenReturn(Optional.of(seedUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generate(any())).thenReturn("jwt");

        service.authenticate("id-token");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getIdGoogle()).isEqualTo("new-sub");
    }

    @Test
    void authenticate_invalidToken_throws401() {
        when(googleValidator.validate("bad-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.authenticate("bad-token"))
                .isInstanceOf(InvalidCredentialsException.class);
        verifyNoInteractions(userRepository, jwtService);
    }

    @Test
    void authenticate_emptyAdminList_doesNotThrow() {
        var props = new AgrocoreSecurityProperties(
                new AgrocoreSecurityProperties.Jwt("secret", 86400000L),
                List.of(), // lista vacía
                new AgrocoreSecurityProperties.Google("client-id")
        );
        service = new AuthenticationService(googleValidator, userRepository, jwtService, props);
        GoogleIdToken.Payload payload = mockPayload("a@b.com", "sub", "A", null);
        when(googleValidator.validate("t")).thenReturn(Optional.of(payload));
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generate(any())).thenReturn("jwt");

        AuthResult result = service.authenticate("t");
        assertThat(result.user().getRole()).isEqualTo(Role.OPERADOR);
    }

    // -- helper --

    @SuppressWarnings("unchecked")
    private GoogleIdToken.Payload mockPayload(String email, String sub, String name, String picture) {
        GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);
        when(payload.getEmail()).thenReturn(email);
        when(payload.getSubject()).thenReturn(sub);
        when(payload.get("name")).thenReturn(name);
        when(payload.get("picture")).thenReturn(picture);
        return payload;
    }
}
