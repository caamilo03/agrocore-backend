package co.edu.udea.agrocore.backend.domain.port.in;

import co.edu.udea.agrocore.backend.domain.model.OptimalRanges;
import co.edu.udea.agrocore.backend.domain.model.TelemetryStats;

import java.time.Instant;
import java.util.UUID;

public interface GetTelemetryStatsUseCase {
    /**
     * Calcula stats agregadas de las lecturas del lote en el rango
     * {@code [from, to]}, incluyendo el porcentaje de tiempo en rango
     * optimo segun {@code ranges}.
     */
    TelemetryStats getStats(UUID batchId, Instant from, Instant to, OptimalRanges ranges);
}
