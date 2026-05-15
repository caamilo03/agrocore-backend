package co.edu.udea.agrocore.backend.domain.model;

/**
 * Agregado de dominio con toda la trazabilidad de un crop_batch:
 * el lote en si, sus referencias enriquecidas (species, substrate y los
 * dos suppliers), y un resumen estadistico de la telemetria del periodo
 * de cultivo.
 *
 * Cualquier referencia (excepto {@code batch}) puede ser null si la
 * entidad referenciada fue eliminada o si el lote nunca tuvo lecturas
 * de telemetria.
 */
public record TraceabilityView(
        CropBatch batch,
        Species species,
        Substrate substrate,
        Supplier speciesSupplier,
        Supplier substrateSupplier,
        TelemetryStats telemetryStats
) {
}
