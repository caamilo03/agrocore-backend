package co.edu.udea.agrocore.backend.domain.port.in;

import co.edu.udea.agrocore.backend.domain.model.Role;
import co.edu.udea.agrocore.backend.domain.model.User;
import java.util.UUID;

public interface ChangeUserRoleUseCase {
    User changeRole(UUID userId, Role newRole);
}
