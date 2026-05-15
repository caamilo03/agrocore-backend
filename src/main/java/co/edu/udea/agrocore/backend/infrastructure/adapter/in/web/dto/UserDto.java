package co.edu.udea.agrocore.backend.infrastructure.adapter.in.web.dto;

import co.edu.udea.agrocore.backend.domain.model.Role;
import co.edu.udea.agrocore.backend.domain.model.User;

import java.util.UUID;

/** Representacion del usuario en respuestas REST. */
public record UserDto(UUID id, String username, String email, String avatarUrl, Role role) {

    public static UserDto from(User user) {
        return new UserDto(user.getId(), user.getUsername(), user.getEmail(),
                user.getAvatarUrl(), user.getRole());
    }
}
