package co.edu.udea.agrocore.backend.domain.port.in;

import co.edu.udea.agrocore.backend.domain.model.Species;
import java.util.UUID;

public interface UpdateSpeciesUseCase {
    Species update(UUID id, Species species);
}