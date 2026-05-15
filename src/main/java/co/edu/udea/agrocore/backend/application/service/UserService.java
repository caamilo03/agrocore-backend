package co.edu.udea.agrocore.backend.application.service;

import co.edu.udea.agrocore.backend.application.exception.InvalidBatchStateException;
import co.edu.udea.agrocore.backend.domain.model.Role;
import co.edu.udea.agrocore.backend.domain.model.User;
import co.edu.udea.agrocore.backend.domain.port.in.ChangeUserRoleUseCase;
import co.edu.udea.agrocore.backend.domain.port.in.DeleteUserUseCase;
import co.edu.udea.agrocore.backend.domain.port.in.ListUsersUseCase;
import co.edu.udea.agrocore.backend.domain.port.out.UserRepositoryPort;
import co.edu.udea.agrocore.backend.infrastructure.security.AuthenticatedUser;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class UserService implements ListUsersUseCase, ChangeUserRoleUseCase, DeleteUserUseCase {

    private final UserRepositoryPort userRepository;
    private final AuthenticatedUser authenticatedUser;

    public UserService(UserRepositoryPort userRepository, AuthenticatedUser authenticatedUser) {
        this.userRepository = userRepository;
        this.authenticatedUser = authenticatedUser;
    }

    @Override
    public List<User> listAll() {
        return userRepository.findAll();
    }

    @Override
    public User changeRole(UUID userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
        user.setRole(newRole);
        return userRepository.save(user);
    }

    @Override
    public void delete(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("Usuario no encontrado");
        }
        // Un admin no puede eliminarse a sí mismo.
        authenticatedUser.getUserId().ifPresent(currentId -> {
            if (currentId.equals(userId)) {
                throw new InvalidBatchStateException("No puedes eliminar tu propio usuario");
            }
        });
        userRepository.deleteById(userId);
    }
}
