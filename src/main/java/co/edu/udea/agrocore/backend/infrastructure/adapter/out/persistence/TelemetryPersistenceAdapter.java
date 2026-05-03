package co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence;

import co.edu.udea.agrocore.backend.domain.model.TelemetryReading;
import co.edu.udea.agrocore.backend.domain.port.out.TelemetryRepositoryPort;
import co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.entity.TelemetryReadingEntity;
import co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.repository.JpaTelemetryRepository;
import org.springframework.stereotype.Component;

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
