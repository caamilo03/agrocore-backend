package co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.repository;

import co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.entity.TelemetryReadingEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaTelemetryRepository extends JpaRepository<TelemetryReadingEntity, Long> {

    Optional<TelemetryReadingEntity> findFirstByIdCropBatchOrderByRecordedAtDesc(UUID idCropBatch);

    List<TelemetryReadingEntity> findByIdCropBatchOrderByRecordedAtDesc(UUID idCropBatch, Pageable pageable);

    List<TelemetryReadingEntity> findByIdCropBatchAndRecordedAtBetweenOrderByRecordedAtAsc(
            UUID idCropBatch, Instant from, Instant to, Pageable pageable);

    /**
     * Downsampling temporal: agrupa las lecturas del rango por {@code granularity}
     * ('hour' o 'day') y devuelve el promedio de cada bucket. Cada fila trae:
     * <pre>[0] bucket_at (TIMESTAMPTZ, MIN del bucket)
     * [1] avg_temperature (NUMERIC)
     * [2] avg_humidity (NUMERIC)
     * [3] avg_co2 (NUMERIC)</pre>
     *
     * El CAST a TEXT es necesario porque date_trunc no acepta parametros
     * binarios directamente; pgjdbc envia ? como UNKNOWN y Postgres se queja.
     */
    @Query(value = """
            SELECT
              MIN(recorded_at) AS bucket_at,
              AVG(temperature) AS avg_temperature,
              AVG(humidity) AS avg_humidity,
              AVG(co2) AS avg_co2
            FROM telemetry_reading
            WHERE id_crop_batch = :batchId
              AND recorded_at BETWEEN :from AND :to
            GROUP BY date_trunc(CAST(:granularity AS TEXT), recorded_at)
            ORDER BY bucket_at ASC
            LIMIT :maxBuckets
            """, nativeQuery = true)
    List<Object[]> findRepresentativeBuckets(
            @Param("batchId") UUID batchId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("granularity") String granularity,
            @Param("maxBuckets") int maxBuckets);

    /**
     * Agrega las lecturas a un unico tuple con count + avg/min/max y % en
     * rango optimo de cada variable. Devuelve siempre 1 fila (los AVG/MIN/MAX
     * son null si count == 0).
     *
     * Columnas devueltas (en orden):
     * <pre>[0] count                       (BIGINT)
     * [1] avg_temperature             (NUMERIC)
     * [2] min_temperature             (NUMERIC)
     * [3] max_temperature             (NUMERIC)
     * [4] temperature_in_range_pct    (NUMERIC, 0-100)
     * [5..7]  humidity avg/min/max
     * [8]     humidity_in_range_pct
     * [9..11] co2 avg/min/max
     * [12]    co2_in_range_pct</pre>
     */
    @Query(value = """
            SELECT
              COUNT(*),
              AVG(temperature), MIN(temperature), MAX(temperature),
              AVG(CASE WHEN temperature BETWEEN :tMin AND :tMax THEN 1.0 ELSE 0.0 END) * 100,
              AVG(humidity), MIN(humidity), MAX(humidity),
              AVG(CASE WHEN humidity BETWEEN :hMin AND :hMax THEN 1.0 ELSE 0.0 END) * 100,
              AVG(co2), MIN(co2), MAX(co2),
              AVG(CASE WHEN co2 BETWEEN :cMin AND :cMax THEN 1.0 ELSE 0.0 END) * 100
            FROM telemetry_reading
            WHERE id_crop_batch = :batchId
              AND recorded_at BETWEEN :from AND :to
            """, nativeQuery = true)
    Object[] computeStatsRow(
            @Param("batchId") UUID batchId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("tMin") BigDecimal tMin, @Param("tMax") BigDecimal tMax,
            @Param("hMin") BigDecimal hMin, @Param("hMax") BigDecimal hMax,
            @Param("cMin") BigDecimal cMin, @Param("cMax") BigDecimal cMax);
}
