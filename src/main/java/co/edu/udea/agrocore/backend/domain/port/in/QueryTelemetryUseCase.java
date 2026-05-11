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
}
