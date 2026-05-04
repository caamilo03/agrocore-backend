package co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence;

import co.edu.udea.agrocore.backend.domain.model.CropBatch;
import co.edu.udea.agrocore.backend.domain.port.out.CropBatchRepositoryPort;
import co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.entity.CropBatchEntity;
import co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.repository.JpaCropBatchRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CropBatchPersistenceAdapter implements CropBatchRepositoryPort {

    private final JpaCropBatchRepository jpaRepository;

    public CropBatchPersistenceAdapter(JpaCropBatchRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public CropBatch save(CropBatch domain) {
        CropBatchEntity saved = jpaRepository.save(toEntity(domain));
        return toDomain(saved);
    }

    @Override
    public List<CropBatch> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CropBatch> findByStatus(String status) {
        return jpaRepository.findByStatus(status).stream()
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

    private CropBatchEntity toEntity(CropBatch domain) {
        return CropBatchEntity.builder()
                .id(domain.getId())
                .idSpecies(domain.getIdSpecies())
                .idSubstrate(domain.getIdSubstrate())
                .idSpeciesSupplier(domain.getIdSpeciesSupplier())
                .idSubstrateSupplier(domain.getIdSubstrateSupplier())
                .idUser(domain.getIdUser())
                .startDate(domain.getStartDate())
                .endDate(domain.getEndDate())
                .status(domain.getStatus())
                .yieldKg(domain.getYieldKg())
                .build();
    }

    private CropBatch toDomain(CropBatchEntity entity) {
        return CropBatch.builder()
                .id(entity.getId())
                .idSpecies(entity.getIdSpecies())
                .idSubstrate(entity.getIdSubstrate())
                .idSpeciesSupplier(entity.getIdSpeciesSupplier())
                .idSubstrateSupplier(entity.getIdSubstrateSupplier())
                .idUser(entity.getIdUser())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .status(entity.getStatus())
                .yieldKg(entity.getYieldKg())
                .build();
    }
}