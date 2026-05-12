package co.edu.udea.agrocore.backend.domain.port.in;

import co.edu.udea.agrocore.backend.domain.model.TelemetryReading;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QueryTelemetryUseCase {
    Optional<TelemetryReading> getLatest(UUID idCropBatch);
    List<TelemetryReading> getRecent(UUID idCropBatch, int limit);
    List<TelemetryReading> getInRange(UUID idCropBatch, Instant from, Instant to, int limit);

    /**
     * Variante de {@link #getInRange} que devuelve una muestra
     * uniformemente distribuida del periodo (downsampling temporal) en
     * lugar de las primeras N lecturas. Util para rangos largos donde el
     * cap se gastaria en las primeras horas.
     */
    List<TelemetryReading> getRepresentativeInRange(UUID idCropBatch, Instant from, Instant to, int maxBuckets);
}
