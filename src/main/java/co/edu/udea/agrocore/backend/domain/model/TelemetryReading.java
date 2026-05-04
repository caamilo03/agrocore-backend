package co.edu.udea.agrocore.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryReading {
    private Long id;
    private UUID idCropBatch;
    private LocalDateTime recordedAt;
    private BigDecimal temperature;
    private BigDecimal humidity;
    private BigDecimal co2;
}
