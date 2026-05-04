package co.edu.udea.agrocore.backend.domain.port.in;

import co.edu.udea.agrocore.backend.domain.model.TelemetryReading;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QueryTelemetryUseCase {
    Optional<TelemetryReading> getLatest(UUID idCropBatch);
    List<TelemetryReading> getRecent(UUID idCropBatch, int limit);
    List<TelemetryReading> getInRange(UUID idCropBatch, LocalDateTime from, LocalDateTime to, int limit);
}
