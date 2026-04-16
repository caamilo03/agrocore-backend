package co.edu.udea.agrocore.backend.application.service;

import co.edu.udea.agrocore.backend.domain.model.Species;
import co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.entity.SpeciesEntity;
import co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.repository.JpaSpeciesRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SpeciesService {

    private final JpaSpeciesRepository repository;

    public SpeciesService(JpaSpeciesRepository repository) {
        this.repository = repository;
    }

    public Species createSpecies(Species species) {
        species.setIdSpecies(UUID.randomUUID()); // Generamos el ID automático
        SpeciesEntity entity = toEntity(species);
        SpeciesEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    public List<Species> getAllSpecies() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    public Species updateSpecies(UUID id, Species species) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Uy firma, esa especie no existe");
        }
        species.setIdSpecies(id); // Aseguramos que actualice el que es
        SpeciesEntity saved = repository.save(toEntity(species));
        return toDomain(saved);
    }

    public void deleteSpecies(UUID id) {
        repository.deleteById(id);
    }

    // --- Mapeadores manuales (Para pasar de Dominio a Base de Datos y viceversa) ---
    private SpeciesEntity toEntity(Species domain) {
        return SpeciesEntity.builder()
                .idSpecies(domain.getIdSpecies())
                .name(domain.getName())
                .minTemperature(domain.getMinTemperature())
                .maxTemperature(domain.getMaxTemperature())
                .minHumidity(domain.getMinHumidity())
                .maxHumidity(domain.getMaxHumidity())
                .minCo2(domain.getMinCo2())
                .maxCo2(domain.getMaxCo2())
                .build();
    }

    private Species toDomain(SpeciesEntity entity) {
        return Species.builder()
                .idSpecies(entity.getIdSpecies())
                .name(entity.getName())
                .minTemperature(entity.getMinTemperature())
                .maxTemperature(entity.getMaxTemperature())
                .minHumidity(entity.getMinHumidity())
                .maxHumidity(entity.getMaxHumidity())
                .minCo2(entity.getMinCo2())
                .maxCo2(entity.getMaxCo2())
                .build();
    }
}