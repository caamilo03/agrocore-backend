package co.edu.udea.agrocore.backend.infrastructure.adapter.in.messaging.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Contrato del evento publicado en el topic agrocore.telemetry.readings.v1.
 * El nombre del topic incluye la version (v1); cualquier cambio incompatible
 * de este record debe ir acompanado de un nuevo topic vN.
 */
public record TelemetryEvent(
        UUID idCropBatch,
        LocalDateTime recordedAt,
        BigDecimal temperature,
        BigDecimal humidity,
        BigDecimal co2
) {
}
