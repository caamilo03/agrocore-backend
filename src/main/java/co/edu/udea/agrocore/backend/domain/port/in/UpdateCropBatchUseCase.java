package co.edu.udea.agrocore.backend.domain.port.in;
import co.edu.udea.agrocore.backend.domain.model.CropBatch;
import java.util.UUID;

public interface UpdateCropBatchUseCase {
    CropBatch update(UUID id, CropBatch cropBatch);
}