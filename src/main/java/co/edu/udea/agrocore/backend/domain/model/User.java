package co.edu.udea.agrocore.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private UUID id;
    private String idGoogle;
    private String username;
    private String email;
    private String avatarUrl;
    private Role role;
}
