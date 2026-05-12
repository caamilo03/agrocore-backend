package co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.repository;

import co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.entity.TelemetryReadingEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
