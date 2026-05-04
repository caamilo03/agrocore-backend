package co.edu.udea.agrocore.backend.application.service;

import co.edu.udea.agrocore.backend.domain.model.TelemetryReading;
import co.edu.udea.agrocore.backend.domain.port.in.QueryTelemetryUseCase;
import co.edu.udea.agrocore.backend.domain.port.in.SaveTelemetryReadingUseCase;
import co.edu.udea.agrocore.backend.domain.port.out.TelemetryRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TelemetryService implements SaveTelemetryReadingUseCase, QueryTelemetryUseCase {

    private final TelemetryRepositoryPort repositoryPort;

    public TelemetryService(TelemetryRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public TelemetryReading save(TelemetryReading reading) {
        return repositoryPort.save(reading);
    }

    @Override
    public Optional<TelemetryReading> getLatest(UUID idCropBatch) {
        return repositoryPort.findLatestByBatch(idCropBatch);
    }

    @Override
    public List<TelemetryReading> getRecent(UUID idCropBatch, int limit) {
        return repositoryPort.findRecentByBatch(idCropBatch, limit);
    }

    @Override
    public List<TelemetryReading> getInRange(UUID idCropBatch, LocalDateTime from, LocalDateTime to, int limit) {
        return repositoryPort.findByBatchInRange(idCropBatch, from, to, limit);
    }
}
