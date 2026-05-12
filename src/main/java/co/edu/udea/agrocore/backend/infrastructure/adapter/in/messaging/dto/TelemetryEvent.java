package co.edu.udea.agrocore.backend.infrastructure.adapter.in.messaging.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Contrato del evento publicado en el stream Redis agrocore.telemetry.readings.v1.
 * El nombre del stream incluye la version (v1); cualquier cambio incompatible
 * de este record debe ir acompanado de un nuevo stream vN.
 *
 * recordedAt es un Instant para que el JSON serializado incluya el marcador
 * 'Z' (UTC) y los consumidores no asuman zona horaria.
 */
public record TelemetryEvent(
        UUID idCropBatch,
        Instant recordedAt,
        BigDecimal temperature,
        BigDecimal humidity,
        BigDecimal co2
) {
}
