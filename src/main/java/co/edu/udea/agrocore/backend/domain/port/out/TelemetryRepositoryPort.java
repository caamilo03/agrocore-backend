package co.edu.udea.agrocore.backend.domain.port.out;

import co.edu.udea.agrocore.backend.domain.model.TelemetryReading;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TelemetryRepositoryPort {
    TelemetryReading save(TelemetryReading reading);

    /** Ultima lectura del lote, o vacio si aun no hay lecturas. */
    Optional<TelemetryReading> findLatestByBatch(UUID idCropBatch);

    /** Ultimas N lecturas del lote, ordenadas por recordedAt DESC. */
    List<TelemetryReading> findRecentByBatch(UUID idCropBatch, int limit);

    /** Lecturas del lote en el rango [from, to], ordenadas por recordedAt ASC. */
    List<TelemetryReading> findByBatchInRange(UUID idCropBatch, Instant from, Instant to, int limit);

    /**
     * Lecturas representativas del lote en el rango [from, to], ordenadas por
     * recordedAt ASC. A diferencia de {@link #findByBatchInRange}, hace
     * downsampling temporal (buckets por hora o por dia segun la duracion
     * del rango) para que el resultado cubra todo el periodo en lugar de
     * gastarse el cap en las primeras horas.
     *
     * Cada elemento devuelto tiene id=null porque es un agregado, no una
     * fila real de la tabla.
     */
    List<TelemetryReading> findRepresentativeInRange(UUID idCropBatch, Instant from, Instant to, int maxBuckets);
}
