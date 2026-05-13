package co.edu.udea.agrocore.backend.infrastructure.adapter.in.web.dto;

import co.edu.udea.agrocore.backend.domain.model.*;

/**
 * Shape de respuesta del endpoint GET /api/v1/batches/{id}/traceability.
 *
 * Cualquier subobjeto (excepto {@code batch}) puede ser null si la
 * referencia fue eliminada o, en el caso de {@code telemetryStats}, si
 * el lote no tiene lecturas en su periodo de cultivo.
 */
public record TraceabilityResponse(
        CropBatch batch,
        Species species,
        Substrate substrate,
        Supplier speciesSupplier,
        Supplier substrateSupplier,
        TelemetryStats telemetryStats
) {
    public static TraceabilityResponse from(TraceabilityView view) {
        return new TraceabilityResponse(
                view.batch(),
                view.species(),
                view.substrate(),
                view.speciesSupplier(),
                view.substrateSupplier(),
                view.telemetryStats());
    }
}
