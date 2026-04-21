package co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence;

import co.edu.udea.agrocore.backend.domain.model.Species;
import co.edu.udea.agrocore.backend.domain.port.out.SpeciesRepositoryPort;
import co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.entity.SpeciesEntity;
import co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.repository.JpaSpeciesRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class SpeciesPersistenceAdapter implements SpeciesRepositoryPort {

    private final JpaSpeciesRepository jpaRepository;

    public SpeciesPersistenceAdapter(JpaSpeciesRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Species save(Species domain) {
        SpeciesEntity saved = jpaRepository.save(toEntity(domain));
        return toDomain(saved);
    }

    @Override
    public List<Species> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    // Mapeadores (El traductor de Base de Datos a Dominio y viceversa) ---
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