package co.edu.udea.agrocore.backend.application.service;

import co.edu.udea.agrocore.backend.domain.model.Species;
import co.edu.udea.agrocore.backend.domain.port.in.CreateSpeciesUseCase;
import co.edu.udea.agrocore.backend.domain.port.in.DeleteSpeciesUseCase;
import co.edu.udea.agrocore.backend.domain.port.in.GetAllSpeciesUseCase;
import co.edu.udea.agrocore.backend.domain.port.in.UpdateSpeciesUseCase;
import co.edu.udea.agrocore.backend.domain.port.out.SpeciesRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SpeciesService implements CreateSpeciesUseCase, GetAllSpeciesUseCase, UpdateSpeciesUseCase, DeleteSpeciesUseCase {

    private final SpeciesRepositoryPort repositoryPort;

    public SpeciesService(SpeciesRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public Species create(Species species) {
        species.setIdSpecies(UUID.randomUUID());
        return repositoryPort.save(species);
    }

    @Override
    public List<Species> getAll() {
        return repositoryPort.findAll();
    }

    @Override
    public Species update(UUID id, Species species) {
        if (!repositoryPort.existsById(id)) {
            throw new RuntimeException("Especie no encontrada");
        }
        species.setIdSpecies(id);
        return repositoryPort.save(species);
    }

    @Override
    public void delete(UUID id) {
        repositoryPort.deleteById(id);
    }
}