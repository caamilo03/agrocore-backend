package co.edu.udea.agrocore.backend.domain.port.in;

import co.edu.udea.agrocore.backend.domain.model.TraceabilityView;

import java.util.UUID;

public interface GetCropBatchTraceabilityUseCase {

    /**
     * Devuelve la trazabilidad completa de un lote.
     *
     * @throws java.util.NoSuchElementException si el lote no existe
     */
    TraceabilityView get(UUID batchId);
}
