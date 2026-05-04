package co.edu.udea.agrocore.backend.application.service;

import co.edu.udea.agrocore.backend.domain.model.CropBatch;
import co.edu.udea.agrocore.backend.domain.model.Species;
import co.edu.udea.agrocore.backend.domain.model.TelemetryReading;
import co.edu.udea.agrocore.backend.domain.port.out.CropBatchRepositoryPort;
import co.edu.udea.agrocore.backend.domain.port.out.SpeciesRepositoryPort;
import co.edu.udea.agrocore.backend.domain.port.out.TelemetryEventPublisherPort;
import co.edu.udea.agrocore.backend.infrastructure.config.AgrocoreSimulatorProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Genera lecturas de telemetria sinteticas por cada crop_batch ACTIVO.
 *
 * El valor de cada sensor se calcula con un random walk gaussiano alrededor
 * del valor anterior (sigma proporcional a variancePercent del rango de
 * la especie), lo que produce series de tiempo con apariencia realista en
 * lugar de ruido blanco.
 *
 * Solo se instancia si agrocore.simulator.enabled=true (default true en
 * application.yml). Cuando lleguen sensores reales basta con apagar la
 * propiedad para retirarlo del runtime.
 */
@Service
@ConditionalOnProperty(
        prefix = "agrocore.simulator",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class TelemetrySimulatorService {

    private static final Logger log = LoggerFactory.getLogger(TelemetrySimulatorService.class);
    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final int VALUE_SCALE = 2;

    private final CropBatchRepositoryPort cropBatchPort;
    private final SpeciesRepositoryPort speciesPort;
    private final TelemetryEventPublisherPort publisher;
    private final AgrocoreSimulatorProperties properties;
    private final Clock clock;
    private final Random random;

    private final Map<UUID, TelemetryReading> lastReadings = new ConcurrentHashMap<>();
    private final Map<UUID, Species> speciesCache = new ConcurrentHashMap<>();

    public TelemetrySimulatorService(CropBatchRepositoryPort cropBatchPort,
                                     SpeciesRepositoryPort speciesPort,
                                     TelemetryEventPublisherPort publisher,
                                     AgrocoreSimulatorProperties properties,
                                     Clock clock,
                                     Random random) {
        this.cropBatchPort = cropBatchPort;
        this.speciesPort = speciesPort;
        this.publisher = publisher;
        this.properties = properties;
        this.clock = clock;
        this.random = random;
    }

    @Scheduled(fixedDelayString = "${agrocore.simulator.interval-ms:5000}")
    public void tick() {
        List<CropBatch> active = cropBatchPort.findByStatus(ACTIVE_STATUS);
        if (active.isEmpty()) {
            log.debug("Simulador: sin lotes ACTIVE, nada que publicar");
            return;
        }

        int published = 0;
        for (CropBatch batch : active) {
            Optional<TelemetryReading> reading = generateFor(batch);
            if (reading.isPresent()) {
                publisher.publish(reading.get());
                lastReadings.put(batch.getId(), reading.get());
                published++;
            }
        }
        log.debug("Simulador: {} eventos publicados de {} lotes ACTIVE",
                published, active.size());
    }

    Optional<TelemetryReading> generateFor(CropBatch batch) {
        if (batch.getIdSpecies() == null) {
            return Optional.empty();
        }
        Species species = speciesCache.computeIfAbsent(
                batch.getIdSpecies(),
                id -> speciesPort.findById(id).orElse(null)
        );
        if (species == null || !hasCompleteRanges(species)) {
            return Optional.empty();
        }

        TelemetryReading previous = lastReadings.get(batch.getId());

        BigDecimal temperature = nextValue(
                previous != null ? previous.getTemperature() : midpoint(species.getMinTemperature(), species.getMaxTemperature()),
                species.getMinTemperature(), species.getMaxTemperature()
        );
        BigDecimal humidity = nextValue(
                previous != null ? previous.getHumidity() : midpoint(species.getMinHumidity(), species.getMaxHumidity()),
                species.getMinHumidity(), species.getMaxHumidity()
        );
        BigDecimal co2 = nextValue(
                previous != null ? previous.getCo2() : midpoint(species.getMinCo2(), species.getMaxCo2()),
                species.getMinCo2(), species.getMaxCo2()
        );

        return Optional.of(TelemetryReading.builder()
                .idCropBatch(batch.getId())
                .recordedAt(LocalDateTime.now(clock))
                .temperature(temperature)
                .humidity(humidity)
                .co2(co2)
                .build());
    }

    private boolean hasCompleteRanges(Species s) {
        return s.getMinTemperature() != null && s.getMaxTemperature() != null
                && s.getMinHumidity() != null && s.getMaxHumidity() != null
                && s.getMinCo2() != null && s.getMaxCo2() != null;
    }

    private BigDecimal midpoint(BigDecimal min, BigDecimal max) {
        return min.add(max).divide(BigDecimal.valueOf(2), VALUE_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal nextValue(BigDecimal previous, BigDecimal min, BigDecimal max) {
        double range = max.subtract(min).doubleValue();
        double sigma = range * properties.variancePercent() / 100.0;
        double noise = random.nextGaussian() * sigma;
        double candidate = previous.doubleValue() + noise;
        double clamped = Math.max(min.doubleValue(), Math.min(max.doubleValue(), candidate));
        return BigDecimal.valueOf(clamped).setScale(VALUE_SCALE, RoundingMode.HALF_UP);
    }
}
