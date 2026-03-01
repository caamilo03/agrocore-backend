package co.edu.udea.agrocore.backend.domain.port.in;

import co.edu.udea.agrocore.backend.domain.model.CropBatch;

public interface CreateCropBatchUseCase {
    // Recibe un lote sin ID, le aplica reglas de negocio, y lo devuelve ya creado
    CropBatch create(CropBatch cropBatch);
}