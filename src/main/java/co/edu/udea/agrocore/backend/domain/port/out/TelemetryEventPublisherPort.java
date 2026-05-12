package co.edu.udea.agrocore.backend.domain.port.out;

import co.edu.udea.agrocore.backend.domain.model.TelemetryReading;

public interface TelemetryEventPublisherPort {
    void publish(TelemetryReading reading);
}
