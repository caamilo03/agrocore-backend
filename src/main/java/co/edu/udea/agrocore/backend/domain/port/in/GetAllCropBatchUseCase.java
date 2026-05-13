package co.edu.udea.agrocore.backend.domain.port.in;

import co.edu.udea.agrocore.backend.domain.model.CropBatch;
import co.edu.udea.agrocore.backend.domain.model.CropBatchStatus;

import java.util.List;

public interface GetAllCropBatchUseCase {

    /** Devuelve todos los lotes sin filtrar. */
    List<CropBatch> getAll();

    /**
     * Devuelve los lotes filtrados por estado.
     *
     * @param statusFilter si es null, equivale a {@link #getAll()}.
     */
    List<CropBatch> getAll(CropBatchStatus statusFilter);
}
