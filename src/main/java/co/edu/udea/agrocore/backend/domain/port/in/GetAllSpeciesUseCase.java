package co.edu.udea.agrocore.backend.domain.port.in;

import co.edu.udea.agrocore.backend.domain.model.Species;
import java.util.List;

public interface GetAllSpeciesUseCase {
    List<Species> getAll();
}