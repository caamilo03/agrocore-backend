package co.edu.udea.agrocore.backend.domain.port.in;

import co.edu.udea.agrocore.backend.domain.model.Species;

public interface CreateSpeciesUseCase {
    Species create(Species species);
}