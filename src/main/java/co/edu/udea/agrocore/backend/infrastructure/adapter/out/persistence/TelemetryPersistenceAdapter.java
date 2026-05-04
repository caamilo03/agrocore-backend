package co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence;

import co.edu.udea.agrocore.backend.domain.model.TelemetryReading;
import co.edu.udea.agrocore.backend.domain.port.out.TelemetryRepositoryPort;
import co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.entity.TelemetryReadingEntity;
import co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.repository.JpaTelemetryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TelemetryPersistenceAdapter implements TelemetryRepositoryPort {

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
    public List<TelemetryReading> findByBatchInRange(UUID idCropBatch, LocalDateTime from, LocalDateTime to, int limit) {
        return jpaRepository
                .findByIdCropBatchAndRecordedAtBetweenOrderByRecordedAtAsc(
                        idCropBatch, from, to, PageRequest.of(0, limit))
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
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
