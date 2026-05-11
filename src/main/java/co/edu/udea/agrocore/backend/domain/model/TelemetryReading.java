package co.edu.udea.agrocore.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryReading {
    private Long id;
    private UUID idCropBatch;
    /**
     * Timestamp absoluto (UTC) en el que fue tomada la lectura.
     * Se modela como Instant para que la serializacion JSON incluya el
     * marcador 'Z' y los clientes no tengan que asumir la zona horaria.
     */
    private Instant recordedAt;
    private BigDecimal temperature;
    private BigDecimal humidity;
    private BigDecimal co2;
}
