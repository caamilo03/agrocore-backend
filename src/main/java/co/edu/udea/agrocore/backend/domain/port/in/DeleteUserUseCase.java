package co.edu.udea.agrocore.backend.domain.port.in;

import java.util.UUID;

public interface DeleteUserUseCase {
    void delete(UUID userId);
}
