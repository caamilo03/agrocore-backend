package co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.repository;

import co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.entity.TelemetryReadingEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
