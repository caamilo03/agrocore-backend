package co.edu.udea.agrocore.backend.application.service;

import co.edu.udea.agrocore.backend.domain.model.TelemetryReading;
import co.edu.udea.agrocore.backend.domain.port.in.SaveTelemetryReadingUseCase;
import co.edu.udea.agrocore.backend.domain.port.out.TelemetryRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class TelemetryService implements SaveTelemetryReadingUseCase {

    private final TelemetryRepositoryPort repositoryPort;

    public TelemetryService(TelemetryRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public TelemetryReading save(TelemetryReading reading) {
        return repositoryPort.save(reading);
    }
}
