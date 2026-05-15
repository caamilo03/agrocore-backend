package co.edu.udea.agrocore.backend.application.service;

import co.edu.udea.agrocore.backend.application.exception.InvalidBatchStateException;
import co.edu.udea.agrocore.backend.domain.model.Role;
import co.edu.udea.agrocore.backend.domain.model.User;
import co.edu.udea.agrocore.backend.domain.port.out.UserRepositoryPort;
import co.edu.udea.agrocore.backend.infrastructure.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private static final UUID ADMIN_ID = UUID.fromString("aaaa0000-0000-0000-0000-000000000000");
    private static final UUID OTHER_ID = UUID.fromString("bbbb0000-0000-0000-0000-000000000000");

    private UserRepositoryPort repository;
    private AuthenticatedUser authenticatedUser;
    private UserService service;

    @BeforeEach
    void setUp() {
        repository = mock(UserRepositoryPort.class);
        authenticatedUser = mock(AuthenticatedUser.class);
        when(authenticatedUser.getUserId()).thenReturn(Optional.of(ADMIN_ID));
        service = new UserService(repository, authenticatedUser);
    }

    @Test
    void listAll_delegatesToRepository() {
        User u = user(OTHER_ID, Role.OPERADOR);
        when(repository.findAll()).thenReturn(List.of(u));

        assertThat(service.listAll()).containsExactly(u);
        verify(repository).findAll();
    }

    @Test
    void changeRole_updatesAndReturnsUser() {
        User u = user(OTHER_ID, Role.OPERADOR);
        when(repository.findById(OTHER_ID)).thenReturn(Optional.of(u));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = service.changeRole(OTHER_ID, Role.OBSERVADOR);

        assertThat(result.getRole()).isEqualTo(Role.OBSERVADOR);
        verify(repository).save(u);
    }

    @Test
    void changeRole_throwsNotFoundForMissingUser() {
        when(repository.findById(OTHER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.changeRole(OTHER_ID, Role.ADMIN))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void delete_removesExistingUser() {
        when(repository.existsById(OTHER_ID)).thenReturn(true);

        service.delete(OTHER_ID);

        verify(repository).deleteById(OTHER_ID);
    }

    @Test
    void delete_throwsNotFoundForMissingUser() {
        when(repository.existsById(OTHER_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(OTHER_ID))
                .isInstanceOf(NoSuchElementException.class);
        verify(repository, never()).deleteById(any());
    }

    @Test
    void delete_throwsConflictWhenDeletingSelf() {
        when(repository.existsById(ADMIN_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(ADMIN_ID))
                .isInstanceOf(InvalidBatchStateException.class)
                .hasMessageContaining("propio usuario");
        verify(repository, never()).deleteById(any());
    }

    private User user(UUID id, Role role) {
        return User.builder().id(id).email("x@y.com").username("X").role(role).build();
    }
}
