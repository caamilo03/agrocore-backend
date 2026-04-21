package co.edu.udea.agrocore.backend.domain.port.in;

import java.util.UUID;

public interface DeleteSpeciesUseCase {
    void delete(UUID id);
}