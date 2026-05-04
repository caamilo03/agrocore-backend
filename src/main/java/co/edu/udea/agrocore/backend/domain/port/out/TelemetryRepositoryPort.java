package co.edu.udea.agrocore.backend.domain.port.out;

import co.edu.udea.agrocore.backend.domain.model.TelemetryReading;

import java.time.LocalDateTime;
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
    List<TelemetryReading> findByBatchInRange(UUID idCropBatch, LocalDateTime from, LocalDateTime to, int limit);
}
