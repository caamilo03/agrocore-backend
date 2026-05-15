package co.edu.udea.agrocore.backend.infrastructure.adapter.in.web;

import co.edu.udea.agrocore.backend.domain.model.Role;
import co.edu.udea.agrocore.backend.domain.port.in.ChangeUserRoleUseCase;
import co.edu.udea.agrocore.backend.domain.port.in.DeleteUserUseCase;
import co.edu.udea.agrocore.backend.domain.port.in.ListUsersUseCase;
import co.edu.udea.agrocore.backend.infrastructure.adapter.in.web.dto.ChangeRoleRequest;
import co.edu.udea.agrocore.backend.infrastructure.adapter.in.web.dto.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Gestión de usuarios — solo accesible por ADMIN.
 */
@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final ListUsersUseCase listUsersUseCase;
    private final ChangeUserRoleUseCase changeUserRoleUseCase;
    private final DeleteUserUseCase deleteUserUseCase;

    public UserController(ListUsersUseCase listUsersUseCase,
                          ChangeUserRoleUseCase changeUserRoleUseCase,
                          DeleteUserUseCase deleteUserUseCase) {
        this.listUsersUseCase = listUsersUseCase;
        this.changeUserRoleUseCase = changeUserRoleUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> listAll() {
        return ResponseEntity.ok(
                listUsersUseCase.listAll().stream().map(UserDto::from).toList());
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<UserDto> changeRole(@PathVariable UUID id,
                                              @RequestBody ChangeRoleRequest request) {
        Role newRole = Role.fromString(request.role());
        return ResponseEntity.ok(UserDto.from(changeUserRoleUseCase.changeRole(id, newRole)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteUserUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
