package co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.repository;

import co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.entity.TelemetryReadingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaTelemetryRepository extends JpaRepository<TelemetryReadingEntity, Long> {
}
