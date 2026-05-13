package co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence;

import co.edu.udea.agrocore.backend.domain.model.OptimalRanges;
import co.edu.udea.agrocore.backend.domain.model.TelemetryReading;
import co.edu.udea.agrocore.backend.domain.model.TelemetryStats;
import co.edu.udea.agrocore.backend.domain.port.out.TelemetryRepositoryPort;
import co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.entity.TelemetryReadingEntity;
import co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.repository.JpaTelemetryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TelemetryPersistenceAdapter implements TelemetryRepositoryPort {

    /** Umbral en dias para decidir entre bucket horario y diario. */
    private static final long HOURLY_THRESHOLD_DAYS = 7;
    private static final int AGGREGATE_SCALE = 2;

    private final JpaTelemetryRepository jpaRepository;

    public TelemetryPersistenceAdapter(JpaTelemetryRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public TelemetryReading save(TelemetryReading domain) {
        TelemetryReadingEntity saved = jpaRepository.save(toEntity(domain));
        return toDomain(saved);
    }

    @Override
    public Optional<TelemetryReading> findLatestByBatch(UUID idCropBatch) {
        return jpaRepository.findFirstByIdCropBatchOrderByRecordedAtDesc(idCropBatch)
                .map(this::toDomain);
    }

    @Override
    public List<TelemetryReading> findRecentByBatch(UUID idCropBatch, int limit) {
        return jpaRepository
                .findByIdCropBatchOrderByRecordedAtDesc(idCropBatch, PageRequest.of(0, limit))
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TelemetryReading> findByBatchInRange(UUID idCropBatch, Instant from, Instant to, int limit) {
        return jpaRepository
                .findByIdCropBatchAndRecordedAtBetweenOrderByRecordedAtAsc(
                        idCropBatch, from, to, PageRequest.of(0, limit))
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TelemetryReading> findRepresentativeInRange(UUID idCropBatch, Instant from, Instant to, int maxBuckets) {
        String granularity = pickGranularity(from, to);
        List<Object[]> rows = jpaRepository.findRepresentativeBuckets(idCropBatch, from, to, granularity, maxBuckets);
        return rows.stream()
                .map(row -> toDomainBucket(idCropBatch, row))
                .collect(Collectors.toList());
    }

    @Override
    public TelemetryStats computeStats(UUID idCropBatch, Instant from, Instant to, OptimalRanges ranges) {
        Object[] row = jpaRepository.computeStatsRow(
                idCropBatch, from, to,
                ranges.minTemperature(), ranges.maxTemperature(),
                ranges.minHumidity(), ranges.maxHumidity(),
                ranges.minCo2(), ranges.maxCo2());
        return mapStatsRow(row);
    }

    /**
     * Mapea el tuple devuelto por {@code computeStatsRow}. Si count = 0,
     * Postgres devuelve null en los agregados; respetamos esa semantica
     * con {@link TelemetryStats#empty()}.
     *
     * pgjdbc puede envolver el row en otro Object[] dependiendo de la
     * version de Hibernate; aceptamos ambos shapes.
     */
    private static TelemetryStats mapStatsRow(Object[] row) {
        if (row == null || row.length == 0) {
            return TelemetryStats.empty();
        }
        // Algunas versiones devuelven Object[1] que envuelve el row real.
        Object[] r = (row.length == 1 && row[0] instanceof Object[]) ? (Object[]) row[0] : row;
        long count = toLong(r[0]);
        if (count == 0L) {
            return TelemetryStats.empty();
        }
        return new TelemetryStats(
                count,
                scale(r[1]), scale(r[2]), scale(r[3]), scale(r[4]),
                scale(r[5]), scale(r[6]), scale(r[7]), scale(r[8]),
                scale(r[9]), scale(r[10]), scale(r[11]), scale(r[12]));
    }

    private static long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number n) return n.longValue();
        return Long.parseLong(value.toString());
    }

    /** 'hour' si el rango es <= 7 dias, 'day' en otro caso. */
    private static String pickGranularity(Instant from, Instant to) {
        long days = Duration.between(from, to).toDays();
        return days > HOURLY_THRESHOLD_DAYS ? "day" : "hour";
    }

    private TelemetryReading toDomainBucket(UUID idCropBatch, Object[] row) {
        return TelemetryReading.builder()
                .id(null)
                .idCropBatch(idCropBatch)
                .recordedAt(toInstant(row[0]))
                .temperature(scale(row[1]))
                .humidity(scale(row[2]))
                .co2(scale(row[3]))
                .build();
    }

    /**
     * pgjdbc puede devolver el timestamp como java.sql.Timestamp,
     * java.time.OffsetDateTime o java.time.Instant segun la version y el
     * tipo de columna. Aceptamos los tres para que la migracion de
     * TIMESTAMP -> TIMESTAMPTZ no rompa este mapeo.
     */
    private static Instant toInstant(Object value) {
        if (value instanceof Instant i) return i;
        if (value instanceof OffsetDateTime odt) return odt.toInstant();
        if (value instanceof Timestamp ts) return ts.toInstant();
        throw new IllegalStateException("Tipo inesperado para recorded_at: " + value.getClass());
    }

    private static BigDecimal scale(Object value) {
        if (value == null) return null;
        BigDecimal bd = (value instanceof BigDecimal b) ? b : new BigDecimal(value.toString());
        return bd.setScale(AGGREGATE_SCALE, RoundingMode.HALF_UP);
    }

    private TelemetryReadingEntity toEntity(TelemetryReading domain) {
        return TelemetryReadingEntity.builder()
                .id(domain.getId())
                .idCropBatch(domain.getIdCropBatch())
                .recordedAt(domain.getRecordedAt())
                .temperature(domain.getTemperature())
                .humidity(domain.getHumidity())
                .co2(domain.getCo2())
                .build();
    }

    private TelemetryReading toDomain(TelemetryReadingEntity entity) {
        return TelemetryReading.builder()
                .id(entity.getId())
                .idCropBatch(entity.getIdCropBatch())
                .recordedAt(entity.getRecordedAt())
                .temperature(entity.getTemperature())
                .humidity(entity.getHumidity())
                .co2(entity.getCo2())
                .build();
    }
}
