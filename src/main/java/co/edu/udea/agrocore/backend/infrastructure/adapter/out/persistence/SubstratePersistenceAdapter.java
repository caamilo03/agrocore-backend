package co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence;

import co.edu.udea.agrocore.backend.domain.model.Substrate;
import co.edu.udea.agrocore.backend.domain.port.out.SubstrateRepositoryPort;
import co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.entity.SubstrateEntity;
import co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.repository.JpaSubstrateRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class SubstratePersistenceAdapter implements SubstrateRepositoryPort {

    private final JpaSubstrateRepository repository;

    public SubstratePersistenceAdapter(JpaSubstrateRepository repository) {
        this.repository = repository;
    }

    @Override
    public Substrate save(Substrate substrate) {
        SubstrateEntity entity = toEntity(substrate);
        SubstrateEntity savedEntity = repository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Substrate> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Substrate> findAll() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    // Mappers manuales (puedes usar MapStruct si lo tienes configurado)
    private SubstrateEntity toEntity(Substrate domain) {
        SubstrateEntity entity = new SubstrateEntity();
        entity.setIdSubstrate(domain.getIdSubstrate());
        entity.setTypeName(domain.getTypeName());
        entity.setDescription(domain.getDescription());
        return entity;
    }

    private Substrate toDomain(SubstrateEntity entity) {
        return new Substrate(
                entity.getIdSubstrate(),
                entity.getTypeName(),
                entity.getDescription()
        );
    }
}