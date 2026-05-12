package co.edu.udea.agrocore.backend.domain.port.out;

import co.edu.udea.agrocore.backend.domain.model.Species;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpeciesRepositoryPort {
    Species save(Species species);
    List<Species> findAll();
    Optional<Species> findById(UUID id);
    boolean existsById(UUID id);
    void deleteById(UUID id);
}