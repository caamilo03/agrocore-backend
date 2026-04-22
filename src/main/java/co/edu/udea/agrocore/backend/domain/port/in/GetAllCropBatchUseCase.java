package co.edu.udea.agrocore.backend.domain.port.in;
import co.edu.udea.agrocore.backend.domain.model.CropBatch;
import java.util.List;

public interface GetAllCropBatchUseCase {
    List<CropBatch> getAll();
}