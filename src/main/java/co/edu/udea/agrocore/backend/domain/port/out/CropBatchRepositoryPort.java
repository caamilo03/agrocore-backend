package co.edu.udea.agrocore.backend.domain.port.out;

import co.edu.udea.agrocore.backend.domain.model.CropBatch;
import java.util.List;
import java.util.UUID;

public interface CropBatchRepositoryPort {
    CropBatch save(CropBatch cropBatch);
    List<CropBatch> findAll();
    boolean existsById(UUID id);
    void deleteById(UUID id);
}