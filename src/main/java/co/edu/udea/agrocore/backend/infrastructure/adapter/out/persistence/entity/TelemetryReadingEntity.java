package co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "telemetry_reading")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelemetryReadingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_telemetry")
    private Long id;

    @Column(name = "id_crop_batch")
    private UUID idCropBatch;

    @Column(name = "recorded_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant recordedAt;

    private BigDecimal temperature;

    private BigDecimal humidity;

    private BigDecimal co2;
}
