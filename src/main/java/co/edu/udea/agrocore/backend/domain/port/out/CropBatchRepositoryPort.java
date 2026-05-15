package co.edu.udea.agrocore.backend.domain.port.out;

import co.edu.udea.agrocore.backend.domain.model.CropBatch;
import co.edu.udea.agrocore.backend.domain.model.CropBatchStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CropBatchRepositoryPort {
    CropBatch save(CropBatch cropBatch);
    List<CropBatch> findAll();
    Optional<CropBatch> findById(UUID id);
    List<CropBatch> findByStatus(CropBatchStatus status);
    boolean existsById(UUID id);
    void deleteById(UUID id);

    /** Lotes de un usuario específico, con filtro opcional de status. */
    List<CropBatch> findByUserId(UUID userId, CropBatchStatus statusFilter);
}
