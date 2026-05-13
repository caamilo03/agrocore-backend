package co.edu.udea.agrocore.backend.application.service;

import co.edu.udea.agrocore.backend.domain.model.*;
import co.edu.udea.agrocore.backend.domain.port.in.GetCropBatchTraceabilityUseCase;
import co.edu.udea.agrocore.backend.domain.port.out.*;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

/**
 * Compone la vista de trazabilidad: lote + species + substrate + suppliers
 * + stats de telemetria. Referencias eliminadas se devuelven como null
 * (no se rompe el endpoint). Si la especie no tiene rangos optimos
 * completos, las stats no se calculan y se devuelve null.
 */
@Service
public class CropBatchTraceabilityService implements GetCropBatchTraceabilityUseCase {

    private final CropBatchRepositoryPort cropBatchPort;
    private final SpeciesRepositoryPort speciesPort;
    private final SubstrateRepositoryPort substratePort;
    private final SupplierRepositoryPort supplierPort;
    private final TelemetryRepositoryPort telemetryPort;
    private final Clock clock;

    public CropBatchTraceabilityService(CropBatchRepositoryPort cropBatchPort,
                                        SpeciesRepositoryPort speciesPort,
                                        SubstrateRepositoryPort substratePort,
                                        SupplierRepositoryPort supplierPort,
                                        TelemetryRepositoryPort telemetryPort,
                                        Clock clock) {
        this.cropBatchPort = cropBatchPort;
        this.speciesPort = speciesPort;
        this.substratePort = substratePort;
        this.supplierPort = supplierPort;
        this.telemetryPort = telemetryPort;
        this.clock = clock;
    }

    @Override
    public TraceabilityView get(UUID batchId) {
        CropBatch batch = cropBatchPort.findById(batchId)
                .orElseThrow(() -> new NoSuchElementException("Lote no encontrado"));

        Species species = findOrNull(batch.getIdSpecies(), speciesPort::findById);
        Substrate substrate = findOrNull(batch.getIdSubstrate(), substratePort::findById);
        Supplier speciesSupplier = findOrNull(batch.getIdSpeciesSupplier(), supplierPort::findById);
        Supplier substrateSupplier = findOrNull(batch.getIdSubstrateSupplier(), supplierPort::findById);

        TelemetryStats stats = computeStatsIfPossible(batch, species);

        return new TraceabilityView(batch, species, substrate, speciesSupplier, substrateSupplier, stats);
    }

    /**
     * Calcula stats solo si:
     * - el lote tiene startDate
     * - la especie tiene los 6 rangos optimos (sin null)
     * Si la especie es null o tiene rangos incompletos, devuelve null
     * (mejor que romper toda la trazabilidad).
     */
    private TelemetryStats computeStatsIfPossible(CropBatch batch, Species species) {
        if (species == null || batch.getStartDate() == null) {
            return null;
        }
        OptimalRanges ranges;
        try {
            ranges = new OptimalRanges(
                    species.getMinTemperature(), species.getMaxTemperature(),
                    species.getMinHumidity(), species.getMaxHumidity(),
                    species.getMinCo2(), species.getMaxCo2());
        } catch (IllegalArgumentException incomplete) {
            return null;
        }
        Instant from = batch.getStartDate().toInstant(ZoneOffset.UTC);
        Instant to = batch.getEndDate() != null
                ? batch.getEndDate().toInstant(ZoneOffset.UTC)
                : Instant.now(clock);

        TelemetryStats stats = telemetryPort.computeStats(batch.getId(), from, to, ranges);
        return stats.count() == 0 ? null : stats;
    }

    /** Resuelve la referencia opcional sin romperse si el id es null o la entidad no existe. */
    private static <T> T findOrNull(UUID id, java.util.function.Function<UUID, Optional<T>> finder) {
        if (id == null) return null;
        return finder.apply(id).orElse(null);
    }

    // Nota: el lote sigue almacenando LocalDateTime en startDate/endDate
    // (no migrado a Instant aun). Asumimos UTC al convertir, consistente
    // con el resto del backend.
}
