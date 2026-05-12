package co.edu.udea.agrocore.backend.domain.port.in;

import co.edu.udea.agrocore.backend.domain.model.TelemetryReading;

public interface SaveTelemetryReadingUseCase {
    TelemetryReading save(TelemetryReading reading);
}
