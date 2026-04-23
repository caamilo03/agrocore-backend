package co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence;

import co.edu.udea.agrocore.backend.domain.model.Supplier;
import co.edu.udea.agrocore.backend.domain.port.out.SupplierRepositoryPort;
import co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.entity.SupplierEntity;
import co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.repository.JpaSupplierRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class SupplierPersistenceAdapter implements SupplierRepositoryPort {

    private final JpaSupplierRepository repository;

    public SupplierPersistenceAdapter(JpaSupplierRepository repository) {
        this.repository = repository;
    }

    @Override
    public Supplier save(Supplier supplier) {
        SupplierEntity entity = toEntity(supplier);
        SupplierEntity savedEntity = repository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Supplier> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Supplier> findAll() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    // Mapeadores manuales
    private SupplierEntity toEntity(Supplier domain) {
        SupplierEntity entity = new SupplierEntity();
        entity.setIdSupplier(domain.getIdSupplier());
        entity.setNameSupplier(domain.getNameSupplier());
        entity.setContactInfo(domain.getContactInfo());
        return entity;
    }

    private Supplier toDomain(SupplierEntity entity) {
        return new Supplier(
                entity.getIdSupplier(),
                entity.getNameSupplier(),
                entity.getContactInfo()
        );
    }
}