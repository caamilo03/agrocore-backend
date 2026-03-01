package co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence;

import co.edu.udea.agrocore.backend.domain.model.CropBatch;
import co.edu.udea.agrocore.backend.domain.port.out.CropBatchRepositoryPort;
import co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.entity.CropBatchEntity;
import co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.repository.JpaCropBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CropBatchPersistenceAdapter implements CropBatchRepositoryPort {

    private final JpaCropBatchRepository jpaRepository;

    @Override
    public CropBatch save(CropBatch cropBatch) {
        //  Generar un ID si el lote es nuevo
        if (cropBatch.getId() == null) {
            cropBatch.setId(UUID.randomUUID());
        }

        //  Convertir del Dominio (Puro) a la Entidad (Base de datos)
        CropBatchEntity entity = CropBatchEntity.builder()
                .id(cropBatch.getId())
                .substrateOrigin(cropBatch.getSubstrateOrigin())
                .startDate(cropBatch.getStartDate())
                .endDate(cropBatch.getEndDate())
                .status(cropBatch.getStatus())
                .build();

        // guardar en PostgreSQL
        CropBatchEntity savedEntity = jpaRepository.save(entity);

        // devolver el objeto convertido de vuelta al Dominio
        return CropBatch.builder()
                .id(savedEntity.getId())
                .substrateOrigin(savedEntity.getSubstrateOrigin())
                .startDate(savedEntity.getStartDate())
                .endDate(savedEntity.getEndDate())
                .status(savedEntity.getStatus())
                .build();
    }

    @Override
    public Optional<CropBatch> findById(UUID id) {
        return jpaRepository.findById(id).map(entity -> CropBatch.builder()
                .id(entity.getId())
                .substrateOrigin(entity.getSubstrateOrigin())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .status(entity.getStatus())
                .build());
    }
}