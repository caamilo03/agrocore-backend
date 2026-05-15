package co.edu.udea.agrocore.backend.application.service;

import co.edu.udea.agrocore.backend.domain.model.OptimalRanges;
import co.edu.udea.agrocore.backend.domain.model.TelemetryReading;
import co.edu.udea.agrocore.backend.domain.model.TelemetryStats;
import co.edu.udea.agrocore.backend.domain.port.in.GetTelemetryStatsUseCase;
import co.edu.udea.agrocore.backend.domain.port.in.QueryTelemetryUseCase;
import co.edu.udea.agrocore.backend.domain.port.in.SaveTelemetryReadingUseCase;
import co.edu.udea.agrocore.backend.domain.port.out.TelemetryRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TelemetryService implements SaveTelemetryReadingUseCase, QueryTelemetryUseCase, GetTelemetryStatsUseCase {

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
    public List<TelemetryReading> getInRange(UUID idCropBatch, Instant from, Instant to, int limit) {
        return repositoryPort.findByBatchInRange(idCropBatch, from, to, limit);
    }

    @Override
    public List<TelemetryReading> getRepresentativeInRange(UUID idCropBatch, Instant from, Instant to, int maxBuckets) {
        return repositoryPort.findRepresentativeInRange(idCropBatch, from, to, maxBuckets);
    }

    @Override
    public TelemetryStats getStats(UUID batchId, Instant from, Instant to, OptimalRanges ranges) {
        return repositoryPort.computeStats(batchId, from, to, ranges);
    }
}
